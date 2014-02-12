/*
 * JBoss, Home of Professional Open Source.
 * Copyright (c) 2013, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.pull.processor;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.PullRequest;
import org.jboss.pull.shared.Bug;
import org.jboss.pull.shared.PullHelper;
import org.jboss.pull.shared.Util;
import org.jboss.pull.shared.spi.PullEvaluator;


/**
 * Pull request processor derived from Jason's pull-player.
 * It checks all the open PRs whether they are merge-able and schedule a merge job on Hudson for them.
 * A merge-able PR must be approved by a comment "review ok" and must comply to org.jboss.pull.shared.PullHelper#isMergeable().
 * It also checks the status of the latest merge job run on Hudson and post comments on github accordingly, etc.
 *
 * @author <a href="mailto:istudens@redhat.com">Ivo Studensky</a>
 * @author Jason T. Greene
 */
public class Processor {

    /** how many PRs can be merged in one batch */
    private static final int MERGE_BATCH_LIMIT = 20;

    private final String BASE_HOST;
    private final String BASE_PORT;
    private final String BASE_URI;

    private final String BASE_URL;
    private final String BASE_JOB_URL;
    private final String PUBLISH_JOB_URL;
    private final String JENKINS_JOB_TOKEN;

    private final String JENKINS_JOB_NAME;
    private final String COMMENT_PRIVATE_LINK;

    private final boolean DRY_RUN;

    private final String targetBranch;
    private final PullHelper helper;

    private final UserList adminList;

    public static void main(String[] argv) throws Exception {
        if (argv.length == 2) {
            try {
                Processor processor = new Processor(argv[0], argv[1]);
                processor.run();
                System.exit(0);
            } catch (Exception e) {
                System.err.println(e);
                e.printStackTrace(System.err);
            }
        } else {
            System.err.println(usage());
        }
        System.exit(1);
    }


    public Processor(final String targetBranchProperty, final String jenkinsJobNameProperty) throws Exception {
        helper = new PullHelper("processor.properties.file", "./processor.properties");

        targetBranch = Util.require(helper.getProps(), targetBranchProperty);
        JENKINS_JOB_NAME = Util.require(helper.getProps(), jenkinsJobNameProperty);

        BASE_HOST = Util.require(helper.getProps(), "jenkins.host");
        BASE_PORT = Util.require(helper.getProps(), "jenkins.port");
        BASE_URI = Util.get(helper.getProps(), "jenkins.uri", "");
        PUBLISH_JOB_URL = Util.require(helper.getProps(), "jenkins.publish.url");
        JENKINS_JOB_TOKEN = Util.get(helper.getProps(), "jenkins.job.token");
        BASE_URL = "http://" + BASE_HOST + ":" + BASE_PORT + BASE_URI;
        BASE_JOB_URL = BASE_URL + "/job";
        COMMENT_PRIVATE_LINK = "Private: " + PUBLISH_JOB_URL + "/" + JENKINS_JOB_NAME + "/";

        adminList = UserList.loadUserList(Util.require(helper.getProps(), "admin.list.file"));

        // system property "dryrun=true"
        DRY_RUN = Boolean.getBoolean("dryrun");
        if (DRY_RUN) {
            System.out.println("Running in a dry run mode.");
        }
    }


    public void run() throws Exception {
        System.out.println("Starting at: " + getTime());
        try {

            // check the status of the merge job
            JenkinsBuild lastBuild = JenkinsBuild.findLastBuild(BASE_URL, JENKINS_JOB_NAME);
            if (lastBuild != null && lastBuild.getStatus() == null) {
                // build is in progress at present, so nothing to do
                System.out.println("Hudson job is still running.");
                return;
            }

            final List<PullRequest> pullRequests = helper.getPullRequestService().getPullRequests(helper.getRepository(), "open");

            final Set<PullRequest> pullsToMerge = new LinkedHashSet<PullRequest>();
            final Set<PullRequest> pullsPending = new LinkedHashSet<PullRequest>();
            final Set<PullRequest> pullsRunning = new LinkedHashSet<PullRequest>();
            final Map<PullRequest, List<String>> pullsToComplain = new LinkedHashMap<PullRequest, List<String>>();

            for (PullRequest pullRequest : pullRequests) {
                if (pullRequest.getHead().getSha() == null) {
                    System.err.printf("Could not get sha1 for pull %d\n", pullRequest.getNumber());
                    continue;
                }

                if (! targetBranch.equals(pullRequest.getBase().getRef())) {
                    continue;
                }

                System.out.printf("number: %d login: %s sha1: %s\n", pullRequest.getNumber(), pullRequest.getUser().getLogin(), pullRequest.getHead().getSha());

                boolean trigger = false;
                boolean running = false;
                boolean pending = false;

                final List<Comment> comments = helper.getIssueService().getComments(helper.getRepository(), pullRequest.getNumber());
                for (Comment comment : comments) {
                    if (helper.getGithubLogin().equals(comment.getUser().getLogin())) {
                        if (helper.isPendingMatched(comment)) {
                            trigger = false;
                            running = false;
                            pending = true;
                            continue;
                        }

                        if (helper.isRunningMatched(comment)) {
                            trigger = false;
                            running = true;
                            pending = false;
                            continue;
                        }

                        if (helper.isFinishedgMatched(comment)) {
                            trigger = false;
                            running = false;
                            pending = false;
                            continue;
                        }
                    }

                    if (helper.isReviewMatched(comment)) {
                        System.out.println("issue updated at: " + getTime(pullRequest.getUpdatedAt()));
                        System.out.println("issue reviewed at: " + getTime(comment.getCreatedAt()));
                        if (pullRequest.getUpdatedAt().compareTo(comment.getCreatedAt()) <= 0
                                && adminList.has(comment.getUser().getLogin())) {
                            // this UpdatedAt doesn't have to be relevant to an update of commit as it takes every change
                            trigger = true;
                            running = false;
                            pending = false;
                        }
                        continue;
                    }
                }

                if (pending) {
                    pullsPending.add(pullRequest);
                } else if (running) {
                    pullsRunning.add(pullRequest);
                } else if (trigger) {
                    // check other conditions, i.e. upstream pull request and bugzilla
                    final PullEvaluator.Result mergeable = helper.isMergeable(pullRequest);
                    if (mergeable.isMergeable()) {
                        pullsToMerge.add(pullRequest);
                    } else {
                        pullsToComplain.put(pullRequest, mergeable.getDescription());
                    }
                } else {
                    Comment lastComment = comments.get(comments.size() - 1);
                    if (helper.isMergeMatched(lastComment) && adminList.has(lastComment.getUser().getLogin())) {
                        pullsToMerge.add(pullRequest);
                    }
                }

            }

            // check the pending pulls and eventually update their state if they have been already started on Hudson
            for (PullRequest pendingPull : pullsPending) {
                JenkinsBuild build = JenkinsBuild.findBuild(BASE_URL, JENKINS_JOB_NAME, Util.map("sha1", pendingPull.getHead().getSha(), "branch", pendingPull.getBase().getRef()));
                if (build != null) {
                    // build in progress
                    notifyBuildRunning(pendingPull, build.getBuild());
                }
            }

            // check the running pulls and eventually update their state according to the result of the Hudson build if it is finished
            for (PullRequest runningPull : pullsRunning) {
                JenkinsBuild build = JenkinsBuild.findBuild(BASE_URL, JENKINS_JOB_NAME, Util.map("sha1", runningPull.getHead().getSha(), "branch", runningPull.getBase().getRef()));
                if (build != null && build.getStatus() != null) {
                    // build finished
                    notifyBuildCompleted(runningPull, build.getBuild(), build.getStatus());
                }
            }

            // trigger new merge job if the last one is finished
            if (! pullsToMerge.isEmpty()) {
                if (lastBuild == null || lastBuild.getStatus() != null) {   // should always be true here
                    // build finished, trigger a new one
                    triggerJob(pullsToMerge);
                }
            }

            // complain about PRs which don't follow the rules
            for (Map.Entry<PullRequest, List<String>> pullToComplain : pullsToComplain.entrySet()) {
                complain(pullToComplain.getKey(), pullToComplain.getValue());
            }

        } finally {
            System.out.println("Completed at: " + getTime());
        }
    }

    private void notifyBuildCompleted(PullRequest pull, int buildNumber, String status) {
        String comment = "Build " + buildNumber + " merging " + pull.getHead().getSha() + " to branch " + pull.getBase().getRef() + " has been finished with outcome " + status + ":\n";
        comment += COMMENT_PRIVATE_LINK + buildNumber + "\n";

        String githubStatus = convertJenkinsStatus(status);
        postComment(pull, comment);
        postStatus(pull, buildNumber, githubStatus);

        if ("success".equals(githubStatus)) {
            postComment(pull, "Merged!");

            // update bugzilla state
            if (! DRY_RUN) {
                try {
                    helper.updateBugzillaStatus(pull, Bug.Status.MODIFIED);
                } catch (Exception e) {
                    System.err.printf("Update of status of bugzilla related to pull %d failed because of: %s.\n", pull.getNumber(), e.getMessage());
                    System.err.println("Retry...");
                    try {
                        helper.updateBugzillaStatus(pull, Bug.Status.MODIFIED);
                    } catch (Exception ex) {
                        System.err.printf("Update of status of bugzilla related to pull %d failed again because of: %s.\n", pull.getNumber(), ex.getMessage());
                    }
                }
            }
        }

    }

    private String convertJenkinsStatus(String status) {
        if (status.equalsIgnoreCase("UNSTABLE") || status.equalsIgnoreCase("FAILURE")) {
            return "failure";
        } else if (status.equalsIgnoreCase("SUCCESS")) {
            return "success";
        }
        return "error";
    }

    private void notifyBuildRunning(PullRequest pull, int buildNumber) {
        String comment = "Build " + buildNumber + " merging " + pull.getHead().getSha() + " to branch " + pull.getBase().getRef() + " has been started:\n";
        comment += COMMENT_PRIVATE_LINK + buildNumber + "\n";

        postComment(pull, comment);
        postStatus(pull, buildNumber, "pending");
    }

    private void notifyBuildTriggered(PullRequest pull) {
        String comment = "Build merging " + pull.getHead().getSha() + " to branch " + pull.getBase().getRef() +  " has been triggered:\n";
        comment += COMMENT_PRIVATE_LINK +"\n";

        postComment(pull, comment);
    }

    private void triggerJob(Set<PullRequest> pullsToMerge) {
        HttpURLConnection urlConnection = null;
        try {
            StringBuilder sha1s = new StringBuilder();
            StringBuilder pulls = new StringBuilder();
            String delim = "";
            int count = 0;
            for (PullRequest pull : pullsToMerge) {
                sha1s.append(delim).append(pull.getHead().getSha());
                pulls.append(delim).append(Integer.toString(pull.getNumber()));
                if (! targetBranch.equals(pull.getBase().getRef())) {
                    // this should never happen
                    throw new IllegalStateException("Base branch of a pull request is different to the configured target branch");
                }
                delim = " ";
                if (++count > MERGE_BATCH_LIMIT) {
                    break;
                }
            }
            URL url = new URL(BASE_JOB_URL + "/" + JENKINS_JOB_NAME + "/buildWithParameters?token=" + JENKINS_JOB_TOKEN + "&pull=" + URLEncoder.encode(pulls.toString(), "UTF-8") +"&sha1=" + URLEncoder.encode(sha1s.toString(), "UTF-8") + "&branch=" + URLEncoder.encode(targetBranch, "UTF-8") + "&dryrun=" + DRY_RUN);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            if (urlConnection.getResponseCode() == 200) {
                for (PullRequest pull : pullsToMerge) {
                    notifyBuildTriggered(pull);
                }
            } else {
                System.err.println("Problem triggering build for pulls: " + pullsToMerge + " response code: " + urlConnection.getResponseCode() + " - " + urlConnection.getResponseMessage());
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        } finally {
            try {
                if (urlConnection != null)
                    urlConnection.disconnect();
            } catch (Throwable t) {
            }
        }
    }

    private void complain(PullRequest pull, List<String> description) {
        StringBuilder comment = new StringBuilder("This PR cannot be merged due to non-compliance with the rules of the relevant EAP version.\n");
        comment.append("details:\n");
        for (String detailDesc: description) {
            comment.append(detailDesc).append("\n");
        }

        try {
            final List<Comment> comments = helper.getIssueService().getComments(helper.getRepository(), pull.getNumber());
            Comment lastComment = comments.get(comments.size() - 1);

            if (lastComment.getBody().indexOf("cannot be merged due to non-compliance with the rules") == -1) {
                postComment(pull, comment.toString());
//                postStatus(pull, -1, "failure");
            }
        } catch (IOException e) {
            System.err.printf("Could not get comments for pull %d due to %s\n", pull.getNumber(), e.getMessage());
        }
    }

    private void postStatus(PullRequest pull, int buildNumber, String status) {
        System.out.println("Setting status: " + status + " on sha " + pull.getHead().getSha());
        String targetUrl = PUBLISH_JOB_URL + "/" + JENKINS_JOB_NAME + "/" + buildNumber;

        if (! DRY_RUN) {
            helper.postGithubStatus(pull, targetUrl, status);
        }
    }

    private void postComment(PullRequest pull, String comment) {
        System.out.println("Posting: " + comment);

        if (! DRY_RUN) {
            helper.postGithubComment(pull, comment);
        }
    }

    private String getTime() {
        Date date = new Date();
        return getTime(date);
    }

    private String getTime(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        return dateFormat.format(date);
    }

    private static String usage() {
        StringBuilder usage = new StringBuilder();
        usage.append("java -jar pull-processor-1.0-SNAPSHOT.jar <property name of the target branch on github> <property name of dedicated jenkins merge job>\n\n");
        usage.append("optional system properties:\n");
        usage.append("-Dprocessor.properties.file defaults to \"./processor.properties\"\n");
        usage.append("-Ddryrun=true to run without changing anything, i.e. simulated run, defaults to false\n");
        return usage.toString();
    }
}

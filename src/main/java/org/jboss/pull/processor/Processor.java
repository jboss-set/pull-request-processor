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

import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.PullRequest;
import org.jboss.pull.shared.Bug;
import org.jboss.pull.shared.PullHelper;
import org.jboss.pull.shared.Util;


/**
 * Stolen and derived from Jason's pull-player.
 *
 * @author <a href="mailto:istudens@redhat.com">Ivo Studensky</a>
 * @author Jason T. Greene
 */
public class Processor {

    private static final Pattern MERGE    = Pattern.compile(".*merge\\W+this\\W+please.*", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern REVIEWED = Pattern.compile(".*review\\W+ok.*", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern PENDING  = Pattern.compile(".*Build.*merging.*has\\W+been\\W+triggered.*", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern RUNNING  = Pattern.compile(".*Build.*merging.*has\\W+been\\W+started.*", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    /** how many PRs can be merged in one batch */
    private static final int MERGE_BATCH_LIMIT = 50;

    private String BASE_HOST;
    private String BASE_PORT;
    private String BASE_URI;

    private String BASE_URL;
    private String BASE_JOB_URL;
    private String PUBLISH_JOB_URL;
    private String JENKINS_JOB_TOKEN;

    private String JENKINS_JOB_NAME;
    private String COMMENT_PRIVATE_LINK;

    private boolean DRY_RUN;

    private PullHelper helper;

    public static void main(String[] args) throws Exception {
        try {
            Processor processor = new Processor();
            processor.run();
            System.exit(0);
        } catch (Exception e) {
            System.err.println(e);
            e.printStackTrace(System.err);
        }
        System.exit(1);
    }


    public Processor() throws Exception {
        helper = new PullHelper("processor.properties.file", "./processor.properties");

        BASE_HOST = Util.require(helper.getProps(), "jenkins.host");
        BASE_PORT = Util.require(helper.getProps(), "jenkins.port");
        BASE_URI = Util.get(helper.getProps(), "jenkins.uri", "");
        PUBLISH_JOB_URL = Util.require(helper.getProps(), "jenkins.publish.url");
        JENKINS_JOB_NAME = Util.require(helper.getProps(), "jenkins.job.name");
        JENKINS_JOB_TOKEN = Util.get(helper.getProps(), "jenkins.job.token");
        BASE_URL = "http://" + BASE_HOST + ":" + BASE_PORT + BASE_URI;
        BASE_JOB_URL = BASE_URL + "/job";
        COMMENT_PRIVATE_LINK = "Private: " + PUBLISH_JOB_URL + "/" + JENKINS_JOB_NAME + "/";

        // system property "dry-run=true"
        DRY_RUN = Boolean.getBoolean("dryrun");
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

            final List<PullRequest> pullRequests = helper.getPullRequestService().getPullRequests(helper.getRepositoryEAP(), "open");

            final Set<PullRequest> pullsToMerge = new LinkedHashSet<PullRequest>();
            final Set<PullRequest> pullsPending = new LinkedHashSet<PullRequest>();
            final Set<PullRequest> pullsRunning = new LinkedHashSet<PullRequest>();

            for (PullRequest pullRequest : pullRequests) {
                if (pullRequest.getHead().getSha() == null) {
                    System.err.printf("Could not get sha1 for pull %d\n", pullRequest.getNumber());
                    continue;
                }

                if (! helper.getGithubBranch().equals(pullRequest.getBase().getRef())) {
                    continue;
                }

//                if (! pullRequest.isMergeable()) {
//                    continue;
//                }

                System.out.printf("number: %d login: %s sha1: %s\n", pullRequest.getNumber(), pullRequest.getUser().getLogin(), pullRequest.getHead().getSha());

                boolean trigger = false;
                boolean running = false;
                boolean pending = false;

                final List<Comment> comments = helper.getIssueService().getComments(helper.getRepositoryEAP(), pullRequest.getNumber());
                for (Comment comment : comments) {
                    if (helper.getGithubLogin().equals(comment.getUser().getLogin())) {
                        if (PENDING.matcher(comment.getBody()).matches()) {
                            trigger = false;
                            running = false;
                            pending = true;
                            continue;
                        }

                        if (RUNNING.matcher(comment.getBody()).matches()) {
                            trigger = false;
                            running = true;
                            pending = false;
                            continue;
                        }
                    }

                    if (REVIEWED.matcher(comment.getBody()).matches()) {
                        System.out.println("issue updated at: " + getTime(pullRequest.getUpdatedAt()));
                        System.out.println("issue reviewed at: " + getTime(comment.getCreatedAt()));
                        if (pullRequest.getUpdatedAt().compareTo(comment.getCreatedAt()) <= 0) {
                            // this UpdatedAt cannot be relevant to an update of commit as it takes every change
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
                    if (helper.isMergeable(pullRequest)) {
                        pullsToMerge.add(pullRequest);
                    }
                } else {
                    Comment lastComment = comments.get(comments.size() - 1);
                    if (MERGE.matcher(lastComment.getBody()).matches()) {
                        // TODO check the user login who did this comment
                        pullsToMerge.add(pullRequest);
                    }
                }

            }

            // check the pending pulls and eventually update their state if they have been already started on Hudson
            for (PullRequest pendingPull : pullsPending) {
                JenkinsBuild build = JenkinsBuild.findBuild(BASE_URL, JENKINS_JOB_NAME, Util.map("sha1", pendingPull.getHead().getSha(), "branch", helper.getGithubBranch()));
                if (build != null) {
                    // build in progress
                    notifyBuildRunning(pendingPull, helper.getGithubBranch(), build.getBuild());
                }
            }

            // check the running pulls and eventually update their state according to the result of the Hudson build if it is finished
            for (PullRequest runningPull : pullsRunning) {
                JenkinsBuild build = JenkinsBuild.findBuild(BASE_URL, JENKINS_JOB_NAME, Util.map("sha1", runningPull.getHead().getSha(), "branch", helper.getGithubBranch()));
                if (build != null && build.getStatus() != null) {
                    // build finished
                    notifyBuildCompleted(runningPull, helper.getGithubBranch(), build.getBuild(), build.getStatus());
                }
            }

            // trigger new merge job if the last one is finished
            if (! pullsToMerge.isEmpty()) {
                if (lastBuild == null || lastBuild.getStatus() != null) {   // should always be true here
                    // build finished, trigger a new one
                    triggerJob(pullsToMerge);
                }
            }

        } finally {
            System.out.println("Completed at: " + getTime());
        }
    }

    private void notifyBuildCompleted(PullRequest pull, String branch, int buildNumber, String status) {
        String comment = "Build " + buildNumber + " merging " + pull.getHead().getSha() + " to branch " + branch + " has been finished with outcome " + status + ":\n";
        comment += COMMENT_PRIVATE_LINK + buildNumber + "\n";

        String githubStatus = convertJenkinsStatus(status);
        postComment(pull, comment);
        postStatus(pull, buildNumber, githubStatus);

        if (! DRY_RUN && "success".equals(githubStatus)) {
            postComment(pull, "Merged!");
            // update bugzilla state
            try {
                helper.updateBugzillaStatus(pull, Bug.Status.MODIFIED);
            } catch (Exception e) {
                System.err.printf("Update of status of bugzilla related to pull %d failed.\n", pull.getNumber());
                // TODO what to do here? do retry it?
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

    private void notifyBuildRunning(PullRequest pull, String branch, int buildNumber) {
        String comment = "Build " + buildNumber + " merging " + pull.getHead().getSha() + " to branch " + branch + " has been started:\n";
        comment += COMMENT_PRIVATE_LINK + buildNumber + "\n";

        postComment(pull, comment);
        postStatus(pull, buildNumber, "pending");
    }

    private void notifyBuildTriggered(PullRequest pull, String branch) {
        String comment = "Build merging " + pull.getHead().getSha() + " to branch " + branch +  " has been triggered:\n";
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
                delim = " ";
                if (++count > MERGE_BATCH_LIMIT) {
                    break;
                }
            }
            URL url = new URL(BASE_JOB_URL + "/" + JENKINS_JOB_NAME + "/buildWithParameters?token=" + JENKINS_JOB_TOKEN + "&pull=" + pulls.toString() +"&sha1=" + sha1s.toString() + "&branch=" + helper.getGithubBranch() + "&dryrun=" + DRY_RUN);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            if (urlConnection.getResponseCode() == 200) {
                for (PullRequest pull : pullsToMerge) {
                    notifyBuildTriggered(pull, helper.getGithubBranch());
                }
            } else {
                System.err.println("Problem triggering build for pulls: " + pullsToMerge);
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

    private void postStatus(PullRequest pull, int buildNumber, String status) {
        System.out.println("Setting status: " + status + " on sha " + pull.getHead().getSha());
        String targetUrl = PUBLISH_JOB_URL + "/" + JENKINS_JOB_NAME + "/" + buildNumber;
        helper.postGithubStatus(pull, targetUrl, status);
    }

    private void postComment(PullRequest pull, String comment) {
        System.out.println("Posting: " + comment);
        helper.postGithubComment(pull, comment);
    }

    private String getTime() {
        Date date = new Date();
        return getTime(date);
    }

    private String getTime(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        return dateFormat.format(date);
    }

}

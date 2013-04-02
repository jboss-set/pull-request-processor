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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.CommitStatus;
import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.CommitService;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.PullRequestService;


/**
 * Stolen and derived from Jason's pull-player.
 *
 * @author <a href="mailto:istudens@redhat.com">Ivo Studensky</a>
 * @author Jason T. Greene
 */
public class Processor {

    private static final Pattern MERGE = Pattern.compile(".*merge\\W+this\\W+please.*", Pattern.CASE_INSENSITIVE);
    private static final Pattern PENDING = Pattern.compile(".*Build.*merging.*has\\W+been\\W+triggered.*", Pattern.CASE_INSENSITIVE);
    private static final Pattern RUNNING = Pattern.compile(".*Build.*merging.*has\\W+been\\W+started.*", Pattern.CASE_INSENSITIVE);

    private static String GITHUB_ORGANIZATION;
    private static String GITHUB_REPO;
    private static String GITHUB_LOGIN;
    private static String GITHUB_TOKEN;
    private static String GITHUB_BRANCH;

    private static String BASE_HOST;
    private static String BASE_PORT;

    private static String BASE_URL;
    private static String BASE_JOB_URL;
    private static String PUBLISH_JOB_URL;
    private static String JENKINS_JOB_TOKEN;
    private static String JENKINS_JOB_NAME;
    private static String COMMENT_PRIVATE_LINK;

    private static GitHubClient client;
    private static IRepositoryIdProvider repository;
    private static CommitService commitService;
    private static IssueService issueService;
    private static PullRequestService pullRequestService;

    static {
        Properties props;
        try {
            props = Util.loadProperties();

            GITHUB_ORGANIZATION = Util.require(props, "github.organization");
            GITHUB_REPO = Util.require(props, "github.repo");
            GITHUB_LOGIN = Util.require(props, "github.login");
            GITHUB_TOKEN = Util.get(props, "github.token");
            GITHUB_BRANCH = Util.get(props, "github.branch");

            BASE_HOST = Util.require(props, "jenkins.host");
            BASE_PORT = Util.require(props, "jenkins.port");
            PUBLISH_JOB_URL = Util.require(props, "jenkins.publish.url");
            JENKINS_JOB_NAME = Util.require(props, "jenkins.job.name");
            JENKINS_JOB_TOKEN = Util.require(props, "jenkins.job.token");
            BASE_URL = "http://" + BASE_HOST + ":" + BASE_PORT + "/jenkins";
            BASE_JOB_URL = BASE_URL + "/job";
            COMMENT_PRIVATE_LINK = "Private: " + PUBLISH_JOB_URL + "/" + JENKINS_JOB_NAME + "/";

            // initialize client and services
            client = new GitHubClient();
            if (GITHUB_TOKEN != null && GITHUB_TOKEN.length() > 0)
                client.setOAuth2Token(GITHUB_TOKEN);
            repository = RepositoryId.create(GITHUB_ORGANIZATION, GITHUB_REPO);
            commitService = new CommitService(client);
            issueService = new IssueService(client);
            pullRequestService = new PullRequestService(client);

        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }


    public static void main(String[] args) throws Exception {
        System.out.println("Starting at: " + getTime());

        final List<PullRequest> pullRequests = pullRequestService.getPullRequests(repository, "open");

        final Set<PullRequest> pullsToMerge = new LinkedHashSet<PullRequest>();
        final Set<PullRequest> pullsPending = new LinkedHashSet<PullRequest>();
        final Set<PullRequest> pullsRunning = new LinkedHashSet<PullRequest>();

        for (PullRequest pullRequest : pullRequests) {
            if (pullRequest.getHead().getSha() == null) {
                System.err.printf("Could not get sha1 for pull %d\n", pullRequest.getNumber());
                return;
            }

            if (! GITHUB_BRANCH.equals(pullRequest.getBase().getRef())) {
                return;
            }

            if (! pullRequest.isMergeable()) {
                return;
            }

            final List<Comment> comments = issueService.getComments(repository, pullRequest.getNumber());
            if (comments.size() == 0) {
                return;
            }

            System.out.printf("number: %d login: %s sha1: %s\n", pullRequest.getNumber(), pullRequest.getUser().getLogin(), pullRequest.getHead().getSha());

            Comment lastComment  = comments.get(comments.size() - 1);

            if (MERGE.matcher(lastComment.getBody()).matches()) {
                pullsToMerge.add(pullRequest);
            } else if (GITHUB_LOGIN.equals(lastComment.getUser().getLogin())) {
                if (PENDING.matcher(lastComment.getBody()).matches()) {
                    pullsPending.add(pullRequest);
                } else if (RUNNING.matcher(lastComment.getBody()).matches()) {
                    pullsRunning.add(pullRequest);
                }
            }
        }

        // trigger new merge job if the last one is finished
        JenkinsBuild build = JenkinsBuild.findLastBuild(BASE_URL, JENKINS_JOB_NAME);
        if (build.getStatus() != null) {
            // build finished, trigger a new one
            triggerJob(pullsToMerge);
        }

        // check the pending pulls and eventually update their state if they have been already started on Hudson
        for (PullRequest pendingPull : pullsPending) {
            long cur = System.currentTimeMillis();
            build = JenkinsBuild.findBuild(BASE_URL, JENKINS_JOB_NAME, Util.map("sha1", pendingPull.getHead().getSha(), "branch", GITHUB_BRANCH));
            System.out.println("\tTime to find build: " + (System.currentTimeMillis() - cur));
            if (build != null) {
                // build in progress
                notifyBuildRunning(pendingPull.getHead().getSha(), GITHUB_BRANCH, pendingPull.getNumber(), build.getBuild());
            }
        }

        // check the running pulls and eventually update their state according to the result of the Hudson build if it is finished
        for (PullRequest runningPull : pullsRunning) {
            long cur = System.currentTimeMillis();
            build = JenkinsBuild.findBuild(BASE_URL, JENKINS_JOB_NAME, Util.map("sha1", runningPull.getHead().getSha(), "branch", GITHUB_BRANCH));
            System.out.println("\tTime to find build: " + (System.currentTimeMillis() - cur));
            if (build != null && build.getStatus() != null) {
                // build finished
                notifyBuildCompleted(runningPull.getHead().getSha(), GITHUB_BRANCH, runningPull.getNumber(), build.getBuild(), build.getStatus());
                //TODO update bugzilla state?
            }
        }

        System.out.println("Completed at: " + getTime());
    }

    private static String getTime() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }

    private static void notifyBuildCompleted(String sha1, String branch, int pull, int buildNumber, String status) {
        String comment = "Build " + buildNumber + " merging " + sha1 + " to branch " + branch + " has been finished with outcome " + status + ":\\n";
        comment += COMMENT_PRIVATE_LINK + buildNumber + "\\n";

        postComment(pull, comment);
        postStatus(buildNumber, convertJenkinsStatus(status), sha1);
    }

    private static String convertJenkinsStatus(String status) {
        if (status.equalsIgnoreCase("UNSTABLE") || status.equalsIgnoreCase("FAILURE")) {
            return "failure";
        } else if (status.equalsIgnoreCase("SUCCESS")) {
            return "success";
        }

        return "error";
    }

    private static void notifyBuildRunning(String sha, String branch,  int pull, int buildNumber) {
        String comment = "Build " + buildNumber + " merging " + sha + " to branch " + branch + " has been started:\\n";
        comment += COMMENT_PRIVATE_LINK + buildNumber + "\\n";

        postComment(pull, comment);
        postStatus(buildNumber, "pending", sha);
    }

    private static void notifyBuildTriggered(String sha, String branch, int pull) {
        String comment = "Build merging " + sha + " to branch " + branch +  " has been triggered:\\n";
        comment += COMMENT_PRIVATE_LINK +"\\n";

        postComment(pull, comment);
    }

    private static void triggerJob(Set<PullRequest> pullsToMerge) {
        HttpURLConnection urlConnection = null;
        try {
            StringBuilder sha1s = new StringBuilder();
            StringBuilder pulls = new StringBuilder();
            String delim = "";
            for (PullRequest pull : pullsToMerge) {
                sha1s.append(delim).append(pull.getHead().getSha());
                pulls.append(delim).append(Integer.toString(pull.getNumber()));
                delim = " ";
            }
            URL url = new URL(BASE_JOB_URL + "/" + JENKINS_JOB_NAME + "/buildWithParameters?token=" + JENKINS_JOB_TOKEN + "&pull=" + pulls.toString() +"&sha1=" + sha1s.toString() + "&branch=" + GITHUB_BRANCH);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            if (urlConnection.getResponseCode() == 200) {
                for (PullRequest pull : pullsToMerge) {
                    notifyBuildTriggered(pull.getHead().getSha(), GITHUB_BRANCH, pull.getNumber());
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

    private static void postStatus(int buildNumber, String status, String sha) {
        System.out.println("Setting status: " + status + " on sha " + sha);

        CommitStatus commitStatus = new CommitStatus();
        String jobUrl = PUBLISH_JOB_URL;
        commitStatus.setTargetUrl(jobUrl + "/" + JENKINS_JOB_NAME + "/" + buildNumber);
        commitStatus.setState(status);

        try {
            commitService.createStatus(repository, sha, commitStatus);
        } catch (Exception e) {
            System.err.printf("Problem posting a status build for sha: %s\n", sha);
            e.printStackTrace(System.err);
        }
    }

    private static void postComment(int pullNumber, String comment) {
        System.out.println("Posting: " + comment);
        try {
            issueService.createComment(repository, pullNumber, comment);
        } catch (IOException e) {
            System.err.printf("Problem posting a comment build for pull: %d\n", pullNumber);
            e.printStackTrace(System.err);
        }
    }
}

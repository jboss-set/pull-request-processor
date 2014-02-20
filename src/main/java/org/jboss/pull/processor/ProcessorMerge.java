package org.jboss.pull.processor;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.PullRequest;
import org.jboss.pull.shared.ProcessorPullState;
import org.jboss.pull.shared.Util;
import org.jboss.pull.shared.spi.PullEvaluator;

public class ProcessorMerge extends Processor {

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

    private final String TARGET_BRANCH;

    public ProcessorMerge(final String targetBranchProperty, final String jenkinsJobNameProperty) throws Exception {

        TARGET_BRANCH = Util.require(helper.getProps(), targetBranchProperty);
        JENKINS_JOB_NAME = Util.require(helper.getProps(), jenkinsJobNameProperty);

        BASE_HOST = Util.require(helper.getProps(), "jenkins.host");
        BASE_PORT = Util.require(helper.getProps(), "jenkins.port");
        BASE_URI = Util.get(helper.getProps(), "jenkins.uri", "");
        PUBLISH_JOB_URL = Util.require(helper.getProps(), "jenkins.publish.url");
        JENKINS_JOB_TOKEN = Util.get(helper.getProps(), "jenkins.job.token");
        BASE_URL = "http://" + BASE_HOST + ":" + BASE_PORT + BASE_URI;
        BASE_JOB_URL = BASE_URL + "/job";
        COMMENT_PRIVATE_LINK = "Private: " + PUBLISH_JOB_URL + "/" + JENKINS_JOB_NAME + "/";

    }

    public void run() {
        System.out.println("Starting at: " + Util.getTime());
        try {
            // check the status of the merge job
            JenkinsBuild lastBuild = JenkinsBuild.findLastBuild(BASE_URL, JENKINS_JOB_NAME);
            if (lastBuild != null && lastBuild.getStatus() == null) {
                // build is in progress at present, so nothing to do
                System.out.printf("Hudson job %s is still running.\n", JENKINS_JOB_NAME);
                return;
            }

            final List<PullRequest> pullRequests = helper.getPullRequests("open");

            final Set<PullRequest> pullsToMerge = new LinkedHashSet<PullRequest>();
            final Set<PullRequest> pullsPending = new LinkedHashSet<PullRequest>();
            final Set<PullRequest> pullsRunning = new LinkedHashSet<PullRequest>();
            final Set<PullRequest> pullsToComplain = new LinkedHashSet<PullRequest>();

            for (PullRequest pullRequest : pullRequests) {
                if (pullRequest.getHead().getSha() == null) {
                    System.err.printf("Could not get sha1 for pull %d\n", pullRequest.getNumber());
                    continue;
                }

                if (!TARGET_BRANCH.equals(pullRequest.getBase().getRef())) {
                    continue;
                }

                System.out.printf("number: %d login: %s sha1: %s\n", pullRequest.getNumber(), pullRequest.getUser().getLogin(),
                        pullRequest.getHead().getSha());

                final ProcessorPullState pullRequestState = helper.checkPullRequestState(pullRequest);
                System.out.printf("state: %s\n", pullRequestState);
                switch (pullRequestState) {
                    case PENDING:
                        pullsPending.add(pullRequest);
                        break;
                    case RUNNING:
                        pullsRunning.add(pullRequest);
                        break;
                    case INCOMPLETE:
                        pullsToComplain.add(pullRequest);
                        break;
                    case MERGEABLE:
                        pullsToMerge.add(pullRequest);
                        break;
                    case NEW:
                    case FINISHED:
                    case ERROR:
                        // nothing to do here
                }

            }

            // check the pending pulls and eventually update their state if they have been already started on Hudson
            for (PullRequest pendingPull : pullsPending) {
                JenkinsBuild build = JenkinsBuild.findBuild(BASE_URL, JENKINS_JOB_NAME,
                        Util.map("sha1", pendingPull.getHead().getSha(), "branch", pendingPull.getBase().getRef()));
                if (build != null) {
                    // build in progress
                    notifyBuildRunning(pendingPull, build.getBuild());
                }
            }

            // check the running pulls and eventually update their state according to the result of the Hudson build (if it is
            // finished)
            for (PullRequest runningPull : pullsRunning) {
                JenkinsBuild build = JenkinsBuild.findBuild(BASE_URL, JENKINS_JOB_NAME,
                        Util.map("sha1", runningPull.getHead().getSha(), "branch", runningPull.getBase().getRef()));
                if (build != null && build.getStatus() != null) {
                    // build finished
                    notifyBuildCompleted(runningPull, build.getBuild(), build.getStatus());
                }
            }

            // trigger new merge job if the last one is finished
            if (!pullsToMerge.isEmpty()) {
                if (lastBuild == null || lastBuild.getStatus() != null) { // should always be true here
                    // build finished, trigger a new one
                    triggerJob(pullsToMerge);
                }
            }

            // complain about PRs which don't follow the rules
            for (PullRequest pullToComplain : pullsToComplain) {
                // get details why it is incomplete
                final PullEvaluator.Result mergeable = helper.getEvaluatorFacade().isMergeable(pullToComplain); // FIXME hmm, we
                                                                                                                // need to check
                                                                                                                // this twice :(
                complain(pullToComplain, mergeable.getDescription());
            }
        } finally {
            System.out.println("Completed at: " + Util.getTime());
        }
    }

    private void notifyBuildCompleted(PullRequest pull, int buildNumber, String status) {
        String comment = "Build " + buildNumber + " merging " + pull.getHead().getSha() + " to branch "
                + pull.getBase().getRef() + " has been finished with outcome " + status + ":\n";
        comment += COMMENT_PRIVATE_LINK + buildNumber + "\n";

        String githubStatus = convertJenkinsStatus(status);
        postComment(pull, comment);
        postStatus(pull, buildNumber, githubStatus);

        if ("success".equals(githubStatus)) {
            postComment(pull, "Merged!");

            // update bugzilla/jira state
            if (!DRY_RUN) {
                helper.getEvaluatorFacade().updateIssueAsMerged(pull);
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
        String comment = "Build " + buildNumber + " merging " + pull.getHead().getSha() + " to branch "
                + pull.getBase().getRef() + " has been started:\n";
        comment += COMMENT_PRIVATE_LINK + buildNumber + "\n";

        postComment(pull, comment);
        postStatus(pull, buildNumber, "pending");
    }

    private void notifyBuildTriggered(PullRequest pull) {
        String comment = "Build merging " + pull.getHead().getSha() + " to branch " + pull.getBase().getRef()
                + " has been triggered:\n";
        comment += COMMENT_PRIVATE_LINK + "\n";

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
                if (!TARGET_BRANCH.equals(pull.getBase().getRef())) {
                    // this should never happen
                    throw new IllegalStateException(
                            "Base branch of a pull request is different to the configured target branch");
                }
                delim = " ";
                if (++count > MERGE_BATCH_LIMIT) {
                    break;
                }
            }
            URL url = new URL(BASE_JOB_URL + "/" + JENKINS_JOB_NAME + "/buildWithParameters?token=" + JENKINS_JOB_TOKEN
                    + "&pull=" + URLEncoder.encode(pulls.toString(), "UTF-8") + "&sha1="
                    + URLEncoder.encode(sha1s.toString(), "UTF-8") + "&branch=" + URLEncoder.encode(TARGET_BRANCH, "UTF-8")
                    + "&dryrun=" + DRY_RUN);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            if (urlConnection.getResponseCode() == 200) {
                for (PullRequest pull : pullsToMerge) {
                    notifyBuildTriggered(pull);
                }
            } else {
                System.err.println("Problem triggering build for pulls: " + pullsToMerge + " response code: "
                        + urlConnection.getResponseCode() + " - " + urlConnection.getResponseMessage());
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
        final String pattern = "cannot be merged due to non-compliance with the rules";
        final StringBuilder comment = new StringBuilder("This PR ").append(pattern).append(" of the relevant EAP version.\n");
        comment.append("details:\n");
        for (String detailDesc : description) {
            comment.append(detailDesc).append("\n");
        }

        boolean postIt = true;

        final List<Comment> comments = helper.getPullRequestComments(pull.getNumber());
        if (!comments.isEmpty()) {
            final Comment lastComment = comments.get(comments.size() - 1);
            if (lastComment.getBody().indexOf(pattern) != -1)
                postIt = false;
        }

        if (postIt)
            postComment(pull, comment.toString());
    }

    private void postStatus(PullRequest pull, int buildNumber, String status) {
        System.out.println("Setting status: " + status + " on sha " + pull.getHead().getSha());
        String targetUrl = PUBLISH_JOB_URL + "/" + JENKINS_JOB_NAME + "/" + buildNumber;

        if (!DRY_RUN) {
            helper.postGithubStatus(pull, targetUrl, status);
        }
    }

}

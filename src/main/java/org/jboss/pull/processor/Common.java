package org.jboss.pull.processor;

import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.Label;
import org.jboss.pull.shared.PullHelper;
import org.jboss.pull.shared.connectors.RedhatPullRequest;

public class Common {


    public static final String BRANCHES_PROPERTY = "github.branches";
    public static final String REQUIRED_FLAGS_PROPERTY = "required.bz.flags";

    public static Boolean isDryRun() {
        return Boolean.getBoolean("dryrun");
    }

    public static void addLabel(PullHelper helper, RedhatPullRequest pullRequest, String labelTitle) {
        Label label = helper.getLabel(labelTitle.replace(" ", "+"));
        if (label != null) {
            if (!hasLabel(pullRequest, labelTitle)) {
                if (!isDryRun()) {
                    pullRequest.addLabel(label);
                }
                System.out.println("Adding label " + labelTitle);
            }
        }
    }

    public static void removeLabel(PullHelper helper, RedhatPullRequest pullRequest, String labelTitle) {
        Label label = helper.getLabel(labelTitle.replace(" ", "+"));
        if (label != null) {
            if (hasLabel(pullRequest, labelTitle)) {
                if (!isDryRun()) {
                    pullRequest.removeLabel(label);
                }
                System.out.println("Removing label " + labelTitle);
            }
        }
    }

    protected static boolean hasLabel(RedhatPullRequest pullRequest, String title) {
        for (Label label : pullRequest.getGithubLabels()) {
            if (label.getName().equals(title)) {
                return true;
            }
        }
        return false;
    }

    public static void complain(RedhatPullRequest pullRequest, List<String> description) {
        if (!description.isEmpty()) {
            final String pattern = "This PR cannot be merged. Please edit description or associated links.";
            final StringBuilder comment = new StringBuilder(pattern + "\n");
            for (String detailDesc : description) {
                comment.append("- ").append(detailDesc).append("\n");
            }

            boolean postIt = true;

            Comment lastComplaint = pullRequest.getLastMatchingGithubComment(Pattern.compile(pattern));
            if (lastComplaint != null && Pattern.matches(comment.toString(), lastComplaint.getBody())) {
                System.out.println("Complaint hasn't changed. Not posting.");
                postIt = false;
            }

            if (postIt)
                Common.postComment(pullRequest, comment.toString());
        }
    }

    public static void postComment(RedhatPullRequest pullRequest, String comment) {
        System.out.println("Posting Github Comment:\n\'" + comment + "'");

        if (!Common.isDryRun()) {
            pullRequest.postGithubComment(comment);
        }
    }

}

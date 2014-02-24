package org.jboss.pull.processor;

import java.util.List;

import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.PullRequest;
import org.jboss.pull.shared.Util;

public class ProcessorGithubMilestone extends Processor {

    public ProcessorGithubMilestone() throws Exception {
    }

    public void run() {
        System.out.println("Starting at: " + Util.getTime());

        try {
            final List<PullRequest> pullRequests = helper.getGHHelper().getPullRequests("open");

            for (PullRequest pullRequest : pullRequests) {
                if (pullRequest.getMilestone() == null) {
                    setMilestone(pullRequest);
                }
            }
        } finally {
            System.out.println("Completed at: " + Util.getTime());
        }
    }

    public void setMilestone(PullRequest noMilestone) {
        // Set milestone on PullRequest
        Milestone milestone;

        milestone = findOrCreateMilestone(noMilestone.getBase().getRef());

        org.eclipse.egit.github.core.Issue issue = helper.getGHHelper().getIssue(getIssueIdFromIssueURL(noMilestone.getIssueUrl()));

        issue.setMilestone(milestone);
        if (!DRY_RUN) {
            issue = helper.getGHHelper().editIssue(issue);
        } else {
            System.out.println("DRYRUN: Edit issue with new milestone");
        }
        // Post a comment about it
        postComment(noMilestone, "Milestone changed to '" + milestone.getTitle() + "'");

    }

    private Milestone findOrCreateMilestone(String title) {
        List<Milestone> milestones = helper.getGHHelper().getMilestones();

        for (Milestone milestone : milestones) {
            if (milestone.getTitle().equals(title)) {
                return milestone;
            }
        }
        Milestone milestone = null;
        if (!DRY_RUN) {
            milestone = helper.getGHHelper().createMilestone(title);
        } else {
            milestone = new Milestone().setTitle(title);
            System.out.println("DRYRUN: Creating Milestone: " + title);
        }

        return milestone;
    }

    private int getIssueIdFromIssueURL(String issueURL) {
        return Integer.valueOf(issueURL.substring(issueURL.lastIndexOf("/") + 1));
    }

}

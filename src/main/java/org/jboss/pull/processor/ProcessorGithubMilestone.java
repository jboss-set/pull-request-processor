package org.jboss.pull.processor;

import java.util.List;

import org.eclipse.egit.github.core.Milestone;
import org.jboss.pull.shared.Util;
import org.jboss.pull.shared.connectors.RedhatPullRequest;
import org.jboss.pull.shared.spi.PullEvaluator.Result;

public class ProcessorGithubMilestone extends Processor {

    public ProcessorGithubMilestone() throws Exception {
    }

    public void run() {
        System.out.println("Starting at: " + Util.getTime());

        try {
            final List<RedhatPullRequest> pullRequests = helper.getOpenPullRequests();

            for (RedhatPullRequest pullRequest : pullRequests) {
                if (pullRequest.getMilestone() == null) {
                    setMilestone(pullRequest);
                }
            }
        } finally {
            System.out.println("Completed at: " + Util.getTime());
        }
    }

    @Override
    public Result processPullRequest(RedhatPullRequest pullRequest) {
        // TODO Auto-generated method stub
        return null;
    }

    private void setMilestone(RedhatPullRequest pullRequest) {
        System.out.println("ProcessMilestone processing PullRequest: " + pullRequest.getNumber() + " on repository: "
                + pullRequest.getOrganization() + "/" + pullRequest.getRepository());

        // Set milestone on PullRequest
        Milestone milestone;

        milestone = findOrCreateMilestone(pullRequest.getTargetBranchTitle());

        if (!DRY_RUN) {
            pullRequest.setMilestone(milestone);
        } else {
            System.out.println("DRYRUN: Edit issue with new milestone");
        }
        // Post a comment about it
        postComment(pullRequest, "Milestone changed to '" + milestone.getTitle() + "'");

    }

    private Milestone findOrCreateMilestone(String title) {
        List<Milestone> milestones = helper.getGithubMilestones();

        for (Milestone milestone : milestones) {
            if (milestone.getTitle().equals(title)) {
                return milestone;
            }
        }
        Milestone milestone = null;
        if (!DRY_RUN) {
            milestone = helper.createMilestone(title);
        } else {
            milestone = new Milestone().setTitle(title);
            System.out.println("DRYRUN: Creating Milestone: " + title);
        }

        return milestone;
    }

}

package org.jboss.pull.processor.rules;

import java.util.List;

import org.eclipse.egit.github.core.Milestone;
import org.jboss.pull.processor.Common;
import org.jboss.pull.shared.PullHelper;
import org.jboss.pull.shared.connectors.RedhatPullRequest;
import org.jboss.pull.shared.spi.PullEvaluator.Result;

public class GenericMilestoneRule extends Rule {

    public GenericMilestoneRule(PullHelper helper) {
        super(helper);
    }

    @Override
    public Result processPullRequest(RedhatPullRequest pullRequest, Result result) {
        String defaultTitle = pullRequest.getTargetBranchTitle();

        // Verify milestone is usable
        Milestone milestone = findMilestone(defaultTitle);
        if (!milestoneRule(milestone)) {
            System.out.println("Default milestone: " + defaultTitle + " doesn't exist or is closed. This is wrong.");
        }

        // Establish if milestone can be changed
        if (pullRequest.getMilestone() == null) {
            setMilestone(pullRequest, milestone);
        }

        return result;
    }

    protected boolean milestoneRule(Milestone milestone) {
        if (milestone == null || milestone.getState().equals("closed")) {
            return false;
        }
        return true;
    }

    protected Milestone findMilestone(String title) {
        List<Milestone> milestones = helper.getGithubMilestones();

        for (Milestone milestone : milestones) {
            if (milestone.getTitle().equals(title)) {
                return milestone;
            }
        }

        return null;
    }

    protected void setMilestone(RedhatPullRequest pullRequest, Milestone milestone) {
        if (!Common.isDryRun()) {
            pullRequest.setMilestone(milestone);
        }
        Common.postComment(pullRequest, "Milestone changed to '" + milestone.getTitle() + "'");
    }
}

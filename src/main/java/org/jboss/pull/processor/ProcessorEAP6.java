package org.jboss.pull.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.egit.github.core.Milestone;
import org.jboss.pull.processor.rules.FlagRules;
import org.jboss.pull.shared.Util;
import org.jboss.pull.shared.connectors.RedhatPullRequest;
import org.jboss.pull.shared.connectors.bugzilla.Bug;
import org.jboss.pull.shared.connectors.common.Issue;
import org.jboss.pull.shared.spi.PullEvaluator.Result;

public class ProcessorEAP6 extends Processor {

    public ProcessorEAP6() throws Exception {
    }

    public void run() {
        System.out.println("Starting at: " + Util.getTime());

        try {
            final List<RedhatPullRequest> pullRequests = helper.getOpenPullRequests();

            for (RedhatPullRequest pullRequest : pullRequests) {
                Result result = processPullRequest(pullRequest);

                if (!result.isMergeable()) {
                    complain(pullRequest, result.getDescription());
                } else {
                    System.out.println("No complaints");
                }
            }
        } finally {
            System.out.println("Completed at: " + Util.getTime());
        }
    }

    public Result processPullRequest(RedhatPullRequest pullRequest) {
        System.out.println("\nProcessComplainer processing PullRequest '" + pullRequest.getNumber() + "' on repository '"
                + pullRequest.getOrganization() + "/" + pullRequest.getRepository() + "'");

        Result result = new Result(true);

        defaultConfiguration(pullRequest);

        if (pullRequest.getMilestone() != null && pullRequest.getMilestone().getTitle().equals("on hold")) {
            System.out.println("Github milestone 'on hold'. Do nothing.");
            return result;
        }

        result = bugComplaints(pullRequest, result);
        result = upstreamComplaints(pullRequest, result);

        return result;
    }

    protected void defaultConfiguration(RedhatPullRequest pullRequest) {

        String defaultTitle = pullRequest.getTargetBranchTitle();
        addLabel(pullRequest, defaultTitle);

        // Verify milestone is usable
        Milestone milestone = findMilestone(defaultTitle);
        if (!milestoneRule(milestone)) {
            System.out.println("Default milestone: " + defaultTitle + " doesn't exist or is closed. This is wrong.");
        }

        // Establish if milestone can be changed
        if (pullRequest.getMilestone() == null) {
            setMilestone(pullRequest, milestone);
        }
    }

    protected void setMilestone(RedhatPullRequest pullRequest, Milestone milestone) {
        if (!DRY_RUN) {
            pullRequest.setMilestone(milestone);
        }
        postComment(pullRequest, "Milestone changed to '" + milestone.getTitle() + "'");
    }

    protected Result upstreamComplaints(RedhatPullRequest pullRequest, Result result) {
        // Upstream checks
        if (pullRequest.isUpstreamRequired()) {
            if (pullRequest.hasRelatedPullRequestInDescription()) {
                // Do related PR checks
            } else {
                return result.changeResult(false, Messages.MISSING_UPSTREAM);
            }
        } else {
            System.out.println("Upstream not required");
        }

        return result;
    }

    protected Result bugComplaints(RedhatPullRequest pullRequest, Result result) {
        // Check for a bug
        if (!pullRequest.hasBugLinkInDescription()) {
            return result.changeResult(false, Messages.MISSING_BUG);
        }

        // Multiple Valid Bugs
        List<Bug> matches = getValidBugs(pullRequest);
        if (matches.size() == 0) {
            addLabel(pullRequest, Messages.CHECK_BUG_RELEASE);
            removeLabel(pullRequest, Messages.CHECK_BUG_MILESTONE);
            removeLabel(pullRequest, Messages.getNeedsAck("devel_ack"));
            removeLabel(pullRequest, Messages.getNeedsAck("qa_ack"));
            removeLabel(pullRequest, Messages.getNeedsAck("pm_ack"));
            return result;
        }

        for (Bug bug : matches) {
            System.out.println("Using bug id '" + bug.getNumber() + "' as matching bug.");
            List<String> releases = new ArrayList<String>(bug.getFixVersions());
            if (releases.size() != 1) {
                addLabel(pullRequest, Messages.CHECK_BUG_RELEASE);
            } else {
                removeLabel(pullRequest, Messages.CHECK_BUG_RELEASE);
            }

            checkMilestone(pullRequest, bug, releases.get(0));

            try {
                new FlagRules().processPullRequest(pullRequest, bug);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    private void checkMilestone(RedhatPullRequest pullRequest, Bug bug, String release) {
        // Bug target_milestone is set
        String milestoneTitle = null;
        if (isBugMilestoneSet(bug)) {
            removeLabel(pullRequest, Messages.CHECK_BUG_MILESTONE);
            milestoneTitle = release + "." + bug.getTargetMilestone();
        } else {
            addLabel(pullRequest, Messages.CHECK_BUG_MILESTONE);
            return;
        }

        // Verify milestone is usable
        // If the milestone doesn't exist, or is closed, simply end.
        // Use of specific milestones in Github is optional
        Milestone milestone = findMilestone(milestoneTitle);
        if (!milestoneRule(milestone)) {
            return;
        }

        // Establish if milestone can be changed
        if (pullRequest.getMilestone() == null || pullRequest.getMilestone().getTitle().contains("x")) {
            setMilestone(pullRequest, milestone);
        } else if (!pullRequest.getMilestone().getTitle().equals(milestoneTitle)) {
            System.out.println("Github milestone doesn't match bug milestone.");
        } else {
            System.out.println("Github milestone already matches bug milestone.");
        }
    }

    protected boolean isBugMilestoneSet(Bug bug) {
        String milestone = bug.getTargetMilestone();
        if (!milestone.equals("---") && !milestone.equals("Pending")) {
            return true;
        }
        return false;
    }

    protected String getBranchRegex(RedhatPullRequest pullRequest) {
        String branch = pullRequest.getTargetBranchTitle();
        List<String> branches = helper.getBranches();
        String branchRegex = null;
        if (branch.contains("x")) {
            if (branch.length() == 3) {
                branchRegex = branch.replace("x", "[" + branches.size() + "-9]+");
            } else if (branch.length() == 5) {
                // TODO: Possibly limit regex pattern based on closed github milestones or tags
                branchRegex = branch.replace("x", "[0-9]+");
            }
        }
        return branchRegex;
    }

    protected List<Bug> getValidBugs(RedhatPullRequest pullRequest) {
        String branchRegex = getBranchRegex(pullRequest);

        if (branchRegex != null) {
            List<Issue> bugs = pullRequest.getIssues();
            List<Bug> matches = new ArrayList<Bug>();
            for (Issue bug : bugs) {
                // TODO: Remove when Jira is acceptable
                if (bug instanceof Bug) {
                    List<String> releases = new ArrayList<String>(bug.getFixVersions());
                    for (String release : releases) {
                        if (Pattern.compile(branchRegex).matcher(release).find()) {
                            matches.add((Bug) bug);
                        }
                    }
                }

            }
            return matches;
        } else {
            System.out.println("Branch matching pattern is null. Branch value '" + pullRequest.getTargetBranchTitle()
                    + "' is unusable.");
        }

        return new ArrayList<Bug>();
    }

    protected boolean milestoneRule(Milestone milestone) {
        if (milestone == null || milestone.getState().equals("closed")) {
            return false;
        }
        return true;
    }

    /**
     * Finds a github milestone. Returns null if milestone doesn't exist
     *
     * @param title
     * @return - Milestone found or null
     */
    protected Milestone findMilestone(String title) {
        List<Milestone> milestones = helper.getGithubMilestones();

        for (Milestone milestone : milestones) {
            if (milestone.getTitle().equals(title)) {
                return milestone;
            }
        }

        return null;
    }

}

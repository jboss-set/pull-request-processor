package org.jboss.pull.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.egit.github.core.Milestone;
import org.jboss.pull.shared.Util;
import org.jboss.pull.shared.connectors.RedhatPullRequest;
import org.jboss.pull.shared.connectors.common.Issue;
import org.jboss.pull.shared.connectors.bugzilla.Bug;
import org.jboss.pull.shared.spi.PullEvaluator.Result;

public class ProcessorComplainer extends Processor {

    public ProcessorComplainer() throws Exception {
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

        result = bugComplaints(pullRequest, result);

        // Upstream checks
        if (pullRequest.isUpstreamRequired()) {
            if (hasPullRequestInDescription(pullRequest)) {
                // Do related PR checks
            } else {
                result.setMergeable(false);
                result.addDescription(Messages.MISSING_UPSTREAM);
            }
        } else {
            System.out.println("Upstream not required");
        }

        return result;
    }

    private Result bugComplaints(RedhatPullRequest pullRequest, Result result) {
        // Check for a bug
        if (!pullRequest.isBZInDescription() && !pullRequest.isJiraInDescription()) {
            return result.changeResult(false, Messages.MISSING_BUG);
        }

        // Make sure it's from BZ
        // TODO: Remove when JIRA compatibility is implemented
        if (!pullRequest.isBZInDescription()) {
            System.out.println("JIRA link in description. Currently unable to handle.");
            return result;
        }

        // Ensure only one bug has a valid target_release
        List<Issue> matches = getValidBugs(pullRequest);

        if (matches.isEmpty()) {
            return result.changeResult(false, Messages.NO_MATCHING_BUG);
        } else if (matches.size() > 1) {
            return result.changeResult(false, Messages.MULTIPLE_MATCHING_BUGS);
        }

        Bug bug = (Bug) matches.get(0);
        System.out.println("Using bug id '" + bug.getNumber() + "' as matching bug.");

        // Ensure only one target_release is set
        List<String> releases = new ArrayList<String>(bug.getFixVersions());
        if (releases.size() != 1) {
            return result.changeResult(false, Messages.getMultipleReleases(bug.getNumber()));
        }

        String release = releases.get(0);

        // Bug target_milestone is set
        String milestoneTitle = null;
        if (isBugMilestoneSet(bug)) {
            milestoneTitle = release + "." + bug.getTargetMilestone();
        } else {
            result.changeResult(false, Messages.getMilestoneNotSet(bug.getNumber()));
            if (pullRequest.getMilestone() == null) {
                milestoneTitle = pullRequest.getTargetBranchTitle();
            } else {
                System.out.println("Github milestone: '" + pullRequest.getMilestone().getTitle()
                        + "'. Bug milestone is not set.");
                return result;
            }
        }

        // Verify milestone is usable
        Milestone milestone = findMilestone(milestoneTitle);
        if (milestone == null || milestone.getState().equals("closed")) {
            return result.changeResult(false, Messages.getMilestoneNotExistOrClosed(milestoneTitle));
        }

        // Establish if milestone can be changed
        if (pullRequest.isGithubMilestoneNullOrDefault()) {
            setMilestone(pullRequest, milestone);
        } else if (!pullRequest.getMilestone().getTitle().equals(milestoneTitle)) {
            return result.changeResult(false,
                    Messages.getMilestoneDoesntMatch(pullRequest.getMilestone().getTitle(), milestoneTitle));
        } else {
            System.out.println("Github milestone already matches bug milestone.");
        }

        return result;
    }

    private boolean hasPullRequestInDescription(RedhatPullRequest pullRequest) {
        List<RedhatPullRequest> relatedPullRequests = pullRequest.getRelatedPullRequests();
        if (relatedPullRequests.isEmpty()) {
            return false;
        }
        return true;
    }

    private List<Issue> getValidBugs(RedhatPullRequest pullRequest) {
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

        if (branchRegex != null) {
            List<Issue> bugs = pullRequest.getIssues();
            List<Issue> matches = new ArrayList<Issue>();
            for (Issue bug : bugs) {
                List<String> releases = new ArrayList<String>(bug.getFixVersions());
                for (String release : releases) {
                    if (Pattern.compile(branchRegex).matcher(release).find()) {
                        matches.add(bug);
                    }
                }
            }
            return matches;
        } else {
            System.out.println("Branch matching pattern is null. Branch value '" + branch + "' is unusable.");
        }

        return new ArrayList<Issue>();
    }

    private boolean isBugMilestoneSet(Bug bug) {
        String milestone = bug.getTargetMilestone();
        if (!milestone.equals("---") && !milestone.equals("Pending")) {
            return true;
        }
        return false;
    }

    private void setMilestone(RedhatPullRequest pullRequest, Milestone milestone) {
        if (!DRY_RUN) {
            pullRequest.setMilestone(milestone);
        }

        postComment(pullRequest, "Milestone changed to '" + milestone.getTitle() + "'");
    }

    /**
     * Finds a github milestone. Returns null if milestone doesn't exist
     * @param title
     * @return - Milestone found or null
     */
    private Milestone findMilestone(String title) {
        List<Milestone> milestones = helper.getGithubMilestones();

        for (Milestone milestone : milestones) {
            if (milestone.getTitle().equals(title)) {
                return milestone;
            }
        }

        return null;
    }

    private Milestone createMilestone(String title) {
        System.out.println("Creating Milestone: " + title);

        Milestone milestone = null;
        if (!DRY_RUN) {
            milestone = helper.createMilestone(title);
        } else {
            milestone = new Milestone().setTitle(title);
            milestone.setState("open");
        }
        return milestone;
    }

}

package org.jboss.pull.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.egit.github.core.Milestone;
import org.jboss.pull.shared.Util;
import org.jboss.pull.shared.connectors.RedhatPullRequest;
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
                processPullRequest(pullRequest);
            }
        } finally {
            System.out.println("Completed at: " + Util.getTime());
        }
    }

    public void processPullRequest(RedhatPullRequest pullRequest) {
        Result result = new Result(true);

        System.out.println("\nProcessComplainer processing PullRequest '" + pullRequest.getNumber() + "' on repository '"
                + pullRequest.getOrganization() + "/" + pullRequest.getRepository() + "'");

        // Bug link exists
        if (hasBugInDescription(pullRequest)) {
            // Only continue if bug is BZ
            // TODO: Implement JIRA compatibility
            if (pullRequest.isBZInDescription()) {
                // Only one bug link has valid target_release
                List<Bug> matches = getValidBugs(pullRequest);
                if (matches.size() == 1) {
                    // Only 1 target_release value is set
                    Bug bug = matches.get(0);
                    System.out.println("Using bug id '" + bug.getId() + "' as matching bug.");
                    if (bug.getTargetRelease().size() == 1) {
                        String release = new ArrayList<String>(bug.getTargetRelease()).get(0);
                        // Bug target_milestone is set
                        if (isBugMilestoneSet(bug)) {
                            String bugMilestone = release + "." + bug.getTargetMilestone();
                            Milestone milestone = findOrCreateMilestone(bugMilestone);
                            if (!milestone.getState().equals("closed")) {
                                if (pullRequest.isGithubMilestoneNullOrDefault()) {
                                    setMilestone(pullRequest, milestone);
                                } else if (!pullRequest.getMilestone().getTitle().equals(bugMilestone)) {
                                    result.setMergeable(false);
                                    result.addDescription("Github milestone '" + pullRequest.getMilestone().getTitle()
                                            + "' does not match bug milestone '" + bugMilestone
                                            + "'. Automated process unable to proceed.");
                                } else {
                                    System.out.println("Github milestone already matches bug milestone.");
                                }
                            } else {
                                result.setMergeable(false);
                                result.addDescription("Milestone '" + bugMilestone + "' from bug '" + bug.getId()
                                        + "' has been closed in github.");
                            }
                        } else {
                            if (pullRequest.getMilestone() == null) {
                                Milestone milestone = findOrCreateMilestone(pullRequest.getTargetBranchTitle());
                                if (!milestone.getState().equals("closed")) {
                                    setMilestone(pullRequest, milestone);
                                }else {
                                    result.setMergeable(false);
                                    result.addDescription("Default github milestone '" + pullRequest.getTargetBranchTitle() + "' has been closed. This shouldn't have happened. Sorry.");
                                }
                            }
                            result.setMergeable(false);
                            result.addDescription("Milestone is not set on bug id: " + bug.getId() + ".");
                        }

                    } else {
                        result.setMergeable(false);
                        result.addDescription("Bug id '" + bug.getId()
                                + "' contains multiple target_release values. Please divide into separate bugs.");
                    }
                } else if (matches.isEmpty()) {
                    result.setMergeable(false);
                    result.addDescription("No bug link contains a target_release that matches this branch. Please review bugs in PR description.");
                } else if (matches.size() > 1) {
                    result.setMergeable(false);
                    result.addDescription("Multiple bug links contain a target_release that matches this branch. Please review bugs in PR description.");
                }
            } else {
                System.out.println("JIRA link in description. Currently unable to handle.");
            }
        } else {
            result.setMergeable(false);
            result.addDescription("Missing Bugzilla/JIRA or Target Release/Fix Versions are incompatible. Please add link to description");
        }

        // Upstream checks
        if (pullRequest.isUpstreamRequired()) {
            if (hasPullRequestInDescription(pullRequest)) {
                // Do related PR checks
            } else {
                result.setMergeable(false);
                result.addDescription("Missing Upstream. Please add link to description or indicate 'No upstream required'");
            }
        } else {
            System.out.println("Upstream not required");
        }

        if (!result.isMergeable()) {
            complain(pullRequest, result.getDescription());
        } else {
            System.out.println("No complaints");
        }
    }

    /**
     * Returns true if a BZ or JIRA link is in the description. However, the actual id is not validated.
     *
     * @param pullRequest
     * @return
     */
    private boolean hasBugInDescription(RedhatPullRequest pullRequest) {
        if (pullRequest.isBZInDescription() || pullRequest.isJiraInDescription()) {
            return true;
        }
        return false;
    }

    private boolean hasPullRequestInDescription(RedhatPullRequest pullRequest) {
        List<RedhatPullRequest> relatedPullRequests = pullRequest.getRelatedPullRequests();
        if (relatedPullRequests.isEmpty()) {
            return false;
        }
        return true;
    }

    private List<Bug> getValidBugs(RedhatPullRequest pullRequest) {
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
            List<Bug> bugs = pullRequest.getBugs();
            List<Bug> matches = new ArrayList<Bug>();
            for (Bug bug : bugs) {
                List<String> releases = new ArrayList<String>(bug.getTargetRelease());
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

        return new ArrayList<Bug>();
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

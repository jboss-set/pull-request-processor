package org.jboss.pull.processor.rules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.jboss.pull.processor.Messages;
import org.jboss.pull.processor.Common;
import org.jboss.pull.shared.PullHelper;
import org.jboss.pull.shared.Util;
import org.jboss.pull.shared.connectors.RedhatPullRequest;
import org.jboss.pull.shared.connectors.bugzilla.Bug;
import org.jboss.pull.shared.connectors.common.Flag;
import org.jboss.pull.shared.connectors.common.Issue;
import org.jboss.pull.shared.spi.PullEvaluator.Result;

public class BugRules extends Rule {

    protected Set<String> REQUIRED_FLAGS = new HashSet<String>();

    public BugRules(PullHelper helper) {
        super(helper);
    }

    public void setup(RedhatPullRequest pullRequest) {
        String requiredFlags = Util.get(helper.getProperties(), pullRequest.getTargetBranchTitle() + "."
                + Common.REQUIRED_FLAGS_PROPERTY);
        if (requiredFlags != null) {
            StringTokenizer tokenizer = new StringTokenizer(requiredFlags, ", ");
            while (tokenizer.hasMoreTokens()) {
                String requiredFlag = tokenizer.nextToken();
                REQUIRED_FLAGS.add(requiredFlag);
            }
        }
    }

    public Result processPullRequest(RedhatPullRequest pullRequest, Result result) {
        setup(pullRequest);

        // Check for a bug
        if (!pullRequest.hasBugLinkInDescription()) {
            return result.changeResult(false, Messages.MISSING_BUG);
        }

        // Multiple Valid Bugs
        List<Bug> matches = getValidBugs(pullRequest);
        if (matches.size() == 0) {
            Common.addLabel(helper, pullRequest, Messages.CHECK_BUG_RELEASE);
            Common.removeLabel(helper, pullRequest, Messages.CHECK_BUG_MILESTONE);
            for (String flag : REQUIRED_FLAGS) {
                Common.removeLabel(helper, pullRequest, Messages.getNeedsAck(flag));
            }
            return result;
        }

        for (Bug bug : matches) {
            System.out.println("Using bug id '" + bug.getNumber() + "' as matching bug.");
            List<String> releases = new ArrayList<String>(bug.getFixVersions());
            if (releases.size() != 1) {
                Common.addLabel(helper, pullRequest, Messages.CHECK_BUG_RELEASE);
            } else {
                Common.removeLabel(helper, pullRequest, Messages.CHECK_BUG_RELEASE);
            }

            checkMilestone(pullRequest, bug, releases.get(0));

            checkFlags(pullRequest, bug);
        }

        return result;
    }

    protected void checkFlags(RedhatPullRequest pullRequest, Bug bug) {
        Set<String> flagsToCheck = new HashSet<String>(this.REQUIRED_FLAGS);

        List<Flag> flags = bug.getFlags();
        for (Flag flag : flags) {
            if (flagsToCheck.contains(flag.getName())) {
                if (flag.getStatus() == Flag.Status.POSITIVE) {
                    Common.removeLabel(helper, pullRequest, Messages.getNeedsAck(flag.getName()));
                    flagsToCheck.remove(flag.getName());
                }
            }
        }

        if (!flagsToCheck.isEmpty()) {
            Common.removeLabel(helper, pullRequest, Messages.HAS_ACKS);
            for (String flag : flagsToCheck) {
                Common.addLabel(helper, pullRequest, Messages.getNeedsAck(flag));
            }
        } else {
            Common.addLabel(helper, pullRequest, Messages.HAS_ACKS);
        }
    }

    private void checkMilestone(RedhatPullRequest pullRequest, Bug bug, String release) {
        if (isBugMilestoneSet(bug)) {
            Common.removeLabel(helper, pullRequest, Messages.CHECK_BUG_MILESTONE);
        } else {
            Common.addLabel(helper, pullRequest, Messages.CHECK_BUG_MILESTONE);
        }
    }

    protected boolean isBugMilestoneSet(Bug bug) {
        String milestone = bug.getTargetMilestone();
        if (!milestone.equals("---") && !milestone.equals("Pending")) {
            return true;
        }
        return false;
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
}

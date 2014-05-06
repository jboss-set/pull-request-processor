package org.jboss.pull.processor.rules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

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
    protected Set<String> RELEASE_VALUES = new HashSet<String>();

    public BugRules(PullHelper helper) {
        super(helper);
    }

    public void setup(RedhatPullRequest pullRequest) {
        // Get required flags
        String requiredFlags = Util.get(helper.getProperties(), pullRequest.getTargetBranchTitle() + "."
                + Common.REQUIRED_FLAGS_PROPERTY);
        if (requiredFlags != null) {
            StringTokenizer tokenizer = new StringTokenizer(requiredFlags, ", ");
            while (tokenizer.hasMoreTokens()) {
                String requiredFlag = tokenizer.nextToken();
                REQUIRED_FLAGS.add(requiredFlag);
            }
        }

        // Get branch releases to match bugs
        String releases = Util.require(helper.getProperties(), pullRequest.getTargetBranchTitle() + "."
                + Common.RELEASE_VALUE_PROPERTY);
        if (releases != null) {
            StringTokenizer tokenizer = new StringTokenizer(releases, ", ");
            while (tokenizer.hasMoreTokens()) {
                String release = tokenizer.nextToken();
                RELEASE_VALUES.add(release);
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

        for (String flag : REQUIRED_FLAGS) {
            if (isFlagPositive(bug, flag)) {
                Common.removeLabel(helper, pullRequest, Messages.getNeedsAck(flag));
                flagsToCheck.remove(flag);
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

    protected boolean isFlagPositive(Bug bug, String flagToCheck) {
        List<Flag> flags = bug.getFlags();
        for (Flag flag : flags) {
            if (flagToCheck.contains(flag.getName())) {
                if (flag.getStatus() == Flag.Status.POSITIVE) {
                    return true;
                }
            }
        }
        return false;
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

        List<Issue> bugs = pullRequest.getIssues();
        List<Bug> matches = new ArrayList<Bug>();
        for (Issue bug : bugs) {
            // TODO: Remove when Jira is acceptable
            if (bug instanceof Bug) {

                // If bug contains release that matches a valid release from the configuration file accept it.
                Issue retval = compareTargetRelease(bug);

                if (retval != null) {
                    matches.add((Bug) retval);
                    continue;
                }

                // TODO: Enable?
                // If the target_release doesn't match, check the flags
                // Check Bug Flags
//                Issue retVal = compareFlags(bug);
//                if (retVal != null) {
//                    matches.add((Bug) retVal);
//                    continue;
//                }
            }
        }
        return matches;
    }

    protected Issue compareTargetRelease(Issue bug) {
        List<String> bugReleases = new ArrayList<String>(bug.getFixVersions());
        for (String validRelease : RELEASE_VALUES) {
            for (String bugRelease : bugReleases) {
                if (bugRelease.contains(validRelease)) {
//                    System.out.println("Match target_release: " + validRelease + ":" + bugRelease);
                    return bug;
                }
            }
        }
        return null;
    }

    protected Issue compareFlags(Issue bug) {
        List<Flag> flags = bug.getFlags();
        for (String validRelease : RELEASE_VALUES) {
            for (Flag flag : flags) {
                if (flag.getName().contains(validRelease)) {
                    if (flag.getStatus() == Flag.Status.POSITIVE) {
//                        System.out.println("Match flag: " + validRelease + ":" + flag.getName());
                        return bug;
                    }
                }
            }
        }
        return null;
    }

}

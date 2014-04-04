package org.jboss.pull.processor;

public class ComplaintMessages {

    public static ComplaintMessages MESSAGES = new ComplaintMessages();

    public static final String MISSING_BUG = "Missing Bugzilla or JIRA. Please add link to description";

    public static final String NO_MATCHING_BUG = "No bug link contains a target_release that matches this branch. Please review bugs in PR description.";

    public static final String MULTIPLE_MATCHING_BUGS = "Multiple bug links contain a target_release that matches this branch. Please review bugs in PR description.";

    public static String getMultipleReleases(String bugId) {
        return String.format(MULTIPLE_TARGET_RELEASES, bugId);
    }

    public static final String MULTIPLE_TARGET_RELEASES = "Bug id '%s' contains multiple target_release values. Please divide into separate bugs.";

    public static String getMilestoneNotSet(String bugId) {
        return String.format(MILESTONE_NOT_SET, bugId);
    }

    public static final String MILESTONE_NOT_SET = "Milestone is not set on bug id: %s.";

    public static String getMilestoneNotExistOrClosed(String title) {
        return String.format(MILESTONE_CLOSED, title);
    }

    public static final String MILESTONE_CLOSED = "Milestone '%s' does not exist or has been closed in github.";

    public static String getMilestoneDoesntMatch(String ghMilestone, String bzMilestone){
        return String.format(MILESTONE_DOES_NOT_MATCH, ghMilestone, bzMilestone);
    }
    public static final String MILESTONE_DOES_NOT_MATCH = "Github milestone '%s' does not match bug milestone '%s'. Automated process unable to proceed.";

    public static final String MISSING_UPSTREAM = "Missing Upstream. Please add link to description or indicate 'No upstream required'";
}

package org.jboss.pull.processor;

public class Messages {

    public static Messages MESSAGES = new Messages();

    public static final String MISSING_BUG = "Missing Bugzilla or JIRA. Please add link to description";

    public static final String MISSING_UPSTREAM = "Missing Upstream. Please add link to description or indicate 'No upstream required'";

    public static final String CHECK_BUG_RELEASE = "Check BZ Target Release";

    public static final String CHECK_BUG_MILESTONE = "Check BZ Target Milestone";

    public static String getNeedsAck(String ack) {
        return String.format(NEEDS_ACK, ack);
    }

    private static final String NEEDS_ACK = "Needs %s";

    public static final String HAS_ACKS = "Has All Acks";

    // public static final String NO_MATCHING_BUG =
    // "No bug link contains a target_release that matches this branch. Please review bugs in PR description.";
    //
    // public static final String MULTIPLE_MATCHING_BUGS =
    // "Multiple bug links contain a target_release that matches this branch. Please review bugs in PR description.";
    //
    // public static String getMultipleReleases(String bugId) {
    // return String.format(MULTIPLE_TARGET_RELEASES, bugId);
    // }
    //
    // private static final String MULTIPLE_TARGET_RELEASES =
    // "Bug id '%s' contains multiple target_release values. Please divide into separate bugs.";
    //
    // public static String getMilestoneNotSet(String bugId) {
    // return String.format(MILESTONE_NOT_SET, bugId);
    // }
    //
    // private static final String MILESTONE_NOT_SET = "Milestone is not set on bug id: %s.";
    //
    // public static String getMilestoneNotExistOrClosed(String title) {
    // return String.format(MILESTONE_CLOSED, title);
    // }
    //
    // private static final String MILESTONE_CLOSED = "Bug milestone '%s' does not exist or has been closed in github.";
    //
    // public static String getMilestoneDoesntMatch(String ghMilestone, String bzMilestone){
    // return String.format(MILESTONE_DOES_NOT_MATCH, ghMilestone, bzMilestone);
    // }
    // private static final String MILESTONE_DOES_NOT_MATCH =
    // "Github milestone '%s' does not match bug milestone '%s'. Automated process unable to proceed.";
}

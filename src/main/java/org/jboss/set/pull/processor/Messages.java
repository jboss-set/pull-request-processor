package org.jboss.set.pull.processor;

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

}

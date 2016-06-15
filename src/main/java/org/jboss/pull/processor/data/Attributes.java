package org.jboss.pull.processor.data;

import java.util.List;
import java.util.Map;

public final class Attributes {
    public static final Attribute<Boolean> WRITE_PERMISSION = new Attribute<>("write");
    public static final Attribute<PullRequestData> PULL_REQUEST= new Attribute<>("pullRequest");
    public static final Attribute<List<PullRequestData>> PULL_REQUEST_RELATED= new Attribute<>("pullRequestsRelated");
    public static final Attribute<List<IssueData>> ISSUES_RELATED= new Attribute<>("issuesRelated");
    public static final Attribute<Map<String, List<LabelData>>> LABELS = new Attribute<>("labels");
    public static final Attribute<String> BRANCH = new Attribute<>("branch");
    public static final Attribute<List<IssueData>>  ISSUES_OTHER_STREAMS = new Attribute<>("issuesOtherStreams");
    public static final Attribute<Map<String, Integer>> STATUS = new Attribute<>("status");
    public static final Attribute<LinkData> REPOSITORY = new Attribute<>("repository");
	public static final Attribute<List<String>> STREAMS = new Attribute<>("streams");
	public static final Attribute<List<String>> MESSAGES = new Attribute<>("messages");
}

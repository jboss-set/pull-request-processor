package org.jboss.set.pull.processor.data;

import java.util.List;

public final class Attributes {
    public static final Attribute<Boolean> WRITE_PERMISSION = new Attribute<>("write");

    public static final Attribute<IssueData> ISSUE_CURRENT = new Attribute<>("issue_current");
    public static final Attribute<IssueData> ISSUE_UPSTREAM = new Attribute<>("issue_upstream");
    public static final Attribute<List<IssueData>> ISSUES_RELATED = new Attribute<>("issues_related");

    public static final Attribute<LabelData> LABELS_CURRENT = new Attribute<>("labels_current");
    public static final Attribute<LabelData> LABELS_UPSTREAM = new Attribute<>("labels_upstream");

    public static final Attribute<PullRequestData> PULL_REQUEST_CURRENT = new Attribute<>("pr_current");
    public static final Attribute<PullRequestData> PULL_REQUEST_UPSTREAM = new Attribute<>("pr_upstream");
}
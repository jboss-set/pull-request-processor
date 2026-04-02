package org.jboss.set.pull.processor.impl.evaluator;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

import org.jboss.set.aphrodite.domain.PullRequest;
import org.jboss.set.pull.processor.data.Attributes;
import org.jboss.set.pull.processor.data.EvaluatorData;
import org.jboss.set.pull.processor.data.IssueData;

public class UpstreamIssuesLinkEvaluator extends AbstractIssuesLinkEvaluator {

    @Override
    public List<URI> findIssueURI(PullRequest pullRequest) throws URISyntaxException {
        URI uri = pullRequest.findUpstreamIssueURI();
        return uri != null ? List.of(uri) : Collections.emptyList();
    }

    @Override
    public void setIssueData(EvaluatorData data, List<IssueData> issueData) {
        if (!issueData.isEmpty()) {
            data.setAttributeValue(Attributes.ISSUE_UPSTREAM, issueData.get(0));
        }
    }

}

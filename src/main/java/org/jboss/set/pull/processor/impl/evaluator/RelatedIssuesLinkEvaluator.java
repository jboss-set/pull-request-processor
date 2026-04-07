package org.jboss.set.pull.processor.impl.evaluator;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.jboss.set.aphrodite.domain.PullRequest;
import org.jboss.set.pull.processor.data.Attribute;
import org.jboss.set.pull.processor.data.Attributes;
import org.jboss.set.pull.processor.data.EvaluatorData;
import org.jboss.set.pull.processor.data.IssueData;

public class RelatedIssuesLinkEvaluator extends AbstractIssuesLinkEvaluator {

    @Override
    public List<URI> findIssueURI(PullRequest pullRequest) throws URISyntaxException {
        return pullRequest.findRelatedIssuesURI();
    }

    @Override
    public void setIssueData(EvaluatorData data, List<IssueData> issueData) {
        if (!issueData.isEmpty()) {
            data.setAttributeValue(Attributes.ISSUES_RELATED, issueData);
        }
    }

    @Override
    public List<Attribute<?>> getProducedAttributes() {
        return List.of(Attributes.ISSUES_RELATED);
    }
}

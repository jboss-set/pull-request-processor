package org.jboss.pull.processor.impl.evaluator;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.jboss.pull.processor.Evaluator;
import org.jboss.pull.processor.EvaluatorContext;
import org.jboss.pull.processor.data.Attributes;
import org.jboss.pull.processor.data.EvaluatorData;
import org.jboss.set.aphrodite.domain.PullRequest;

public class UpstreamEvaluator implements Evaluator {

    private Pattern UPSTREAM_NOT_REQUIRED = Pattern.compile(".*no.*upstream.*required.*", Pattern.CASE_INSENSITIVE);

    @Override
    public void eval(EvaluatorContext context, EvaluatorData data) {
        PullRequest pullrequest = context.getPullRequest();
        List<PullRequest> related = context.getRelated();

        if(!UPSTREAM_NOT_REQUIRED.matcher(pullrequest.getBody()).find()) {
            if(!related.isEmpty()) {
                data.setAttributeValue(Attributes.MESSAGES, Arrays.asList("missing upstream issue link"));
            }
        }
    }
}

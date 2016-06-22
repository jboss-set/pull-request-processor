package org.jboss.pull.processor.impl.evaluator;

import java.util.List;
import java.util.stream.Collectors;

import org.jboss.pull.processor.Evaluator;
import org.jboss.pull.processor.EvaluatorContext;
import org.jboss.pull.processor.data.Attributes;
import org.jboss.pull.processor.data.EvaluatorData;
import org.jboss.pull.processor.data.PullRequestData;
import org.jboss.set.aphrodite.domain.Patch;
import org.jboss.set.aphrodite.domain.Stream;

public class PullRequestEvaluator implements Evaluator {

    @Override
    public void eval(EvaluatorContext context, EvaluatorData data) {
        Patch patch = context.getPatch();
        List<Stream> streams = context.getAphrodite().getStreamsBy(patch.getRepository(), patch.getCodebase());
        List<String> streamsStr =  streams.stream().map(e -> e.getName()).collect(Collectors.toList());
        data.setAttributeValue(Attributes.PULL_REQUEST, new PullRequestData(patch.getId(), streamsStr, patch.getURL()));
    }
}

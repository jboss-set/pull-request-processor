package org.jboss.pull.processor.impl.evaluator;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.jboss.pull.processor.Evaluator;
import org.jboss.pull.processor.EvaluatorContext;
import org.jboss.pull.processor.data.Attributes;
import org.jboss.pull.processor.data.EvaluatorData;
import org.jboss.pull.processor.data.PullRequestData;
import org.jboss.set.aphrodite.Aphrodite;
import org.jboss.set.aphrodite.domain.Patch;
import org.jboss.set.aphrodite.domain.Stream;

public class PullRequestRelatedEvaluator implements Evaluator {

    @Override
    public void eval(EvaluatorContext context, EvaluatorData data) {
        List<Patch> relatedPatches = context.getRelated();
        Aphrodite aphrodite = context.getAphrodite();

        List<PullRequestData> links = new ArrayList<>();
        for(Patch patch : relatedPatches) {
            List<Stream> streams = aphrodite.getStreamsBy(patch.getRepository(), patch.getCodebase());
            List<String> streamsStr =  streams.stream().map(e -> e.getName()).collect(Collectors.toList());

            links.add(new PullRequestData(patch.getId(), streamsStr, patch.getURL()));
        }

        data.setAttributeValue(Attributes.PULL_REQUEST_RELATED, links);
    }

}

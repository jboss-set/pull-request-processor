package org.jboss.pull.processor.impl.evaluator;

import java.util.List;
import java.util.stream.Collectors;

import org.jboss.pull.processor.Evaluator;
import org.jboss.pull.processor.EvaluatorContext;
import org.jboss.pull.processor.data.Attributes;
import org.jboss.pull.processor.data.EvaluatorData;
import org.jboss.set.aphrodite.Aphrodite;
import org.jboss.set.aphrodite.domain.Patch;
import org.jboss.set.aphrodite.domain.Stream;

public class StreamsEvaluator implements Evaluator {

    @Override
    public void eval(EvaluatorContext context, EvaluatorData data) {
        Aphrodite aphrodite = context.getAphrodite();
        Patch patch = context.getPatch();
        
        List<Stream> stream = aphrodite.getStreamsBy(patch.getRepository(), patch.getCodebase());
        List<String> streamsStr = stream.stream().map(e -> e.getName()).collect(Collectors.toList());
        data.setAttributeValue(Attributes.STREAMS, streamsStr);
    }

}

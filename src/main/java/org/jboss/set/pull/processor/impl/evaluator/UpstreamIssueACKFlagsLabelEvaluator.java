package org.jboss.set.pull.processor.impl.evaluator;

import java.util.List;

import org.jboss.set.pull.processor.EvaluatorContext;
import org.jboss.set.pull.processor.data.Attribute;
import org.jboss.set.pull.processor.data.Attributes;
import org.jboss.set.pull.processor.data.EvaluatorData;

public class UpstreamIssueACKFlagsLabelEvaluator extends AbstractIssueACKFlagsLabelEvaluator {

    @Override
    public void eval(EvaluatorContext context, EvaluatorData data) {
        processAckLabels(Attributes.ISSUE_UPSTREAM, Attributes.LABELS_UPSTREAM, data);
    }

    @Override
    public List<Attribute<?>> getProducedAttributes() {
        return List.of(Attributes.LABELS_UPSTREAM);
    }

    @Override
    public List<Attribute<?>> getRequiredAttributes() {
        return List.of(Attributes.ISSUE_UPSTREAM);
    }

}

package org.jboss.set.pull.processor.impl.evaluator;

import java.util.List;

import org.jboss.set.pull.processor.EvaluatorContext;
import org.jboss.set.pull.processor.data.Attribute;
import org.jboss.set.pull.processor.data.Attributes;
import org.jboss.set.pull.processor.data.EvaluatorData;

public class CurrentIssueACKFlagsLabelEvaluator extends AbstractIssueACKFlagsLabelEvaluator {

    @Override
    public void eval(EvaluatorContext context, EvaluatorData data) {
        processAckLabels(Attributes.ISSUE_CURRENT, Attributes.LABELS_CURRENT, data);
    }

    @Override
    public List<Attribute<?>> getProducedAttributes() {
        return List.of(Attributes.LABELS_CURRENT);
    }

    @Override
    public List<Attribute<?>> getRequiredAttributes() {
        return List.of(Attributes.ISSUE_CURRENT);
    }

}

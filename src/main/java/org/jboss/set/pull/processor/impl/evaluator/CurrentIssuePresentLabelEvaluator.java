package org.jboss.set.pull.processor.impl.evaluator;

import java.util.List;

import org.jboss.set.pull.processor.EvaluatorContext;
import org.jboss.set.pull.processor.data.Attribute;
import org.jboss.set.pull.processor.data.Attributes;
import org.jboss.set.pull.processor.data.EvaluatorData;
import org.jboss.set.pull.processor.data.DefinedLabelItem.LabelContent;

public class CurrentIssuePresentLabelEvaluator extends AbstractIssuePresentLabelEvaluator {

    @Override
    protected String evaluatorLabel() {
        return "CurrentIssue";
    }

    @Override
    public void eval(EvaluatorContext context, EvaluatorData data) {
        processPresenceLabel(context, Attributes.ISSUE_CURRENT, Attributes.LABELS_CURRENT, LabelContent.Missing_issue, data);
    }

    @Override
    public List<Attribute<?>> getRequiredAttributes() {
        return List.of(Attributes.ISSUE_CURRENT, Attributes.LABELS_CURRENT);
    }
}

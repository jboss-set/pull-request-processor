package org.jboss.set.pull.processor.impl.evaluator;

import java.util.List;

import org.jboss.set.pull.processor.EvaluatorContext;
import org.jboss.set.pull.processor.data.Attribute;
import org.jboss.set.pull.processor.data.Attributes;
import org.jboss.set.pull.processor.data.DefinedLabelItem.LabelContent;
import org.jboss.set.pull.processor.data.EvaluatorData;

public class UpstreamIssuePresentLabelEvaluator extends AbstractIssuePresentLabelEvaluator {

    @Override
    public void eval(EvaluatorContext context, EvaluatorData data) throws Exception {
        processPresenceLabel(Attributes.ISSUE_UPSTREAM, Attributes.LABELS_CURRENT, LabelContent.Missing_upstream_issue, data);
    }

    @Override
    public List<Attribute<?>> getRequiredAttributes() {
        return List.of(Attributes.ISSUE_UPSTREAM, Attributes.LABELS_CURRENT);
    }
}

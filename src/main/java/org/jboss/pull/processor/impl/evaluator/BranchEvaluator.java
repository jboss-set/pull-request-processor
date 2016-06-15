package org.jboss.pull.processor.impl.evaluator;

import org.jboss.pull.processor.Evaluator;
import org.jboss.pull.processor.EvaluatorContext;
import org.jboss.pull.processor.data.Attributes;
import org.jboss.pull.processor.data.EvaluatorData;

public class BranchEvaluator implements Evaluator {

    @Override
    public void eval(EvaluatorContext context, EvaluatorData data) {
        data.setAttributeValue(Attributes.BRANCH, context.getBranch());
    }

}

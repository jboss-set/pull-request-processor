package org.jboss.set.pull.processor.impl.evaluator;

import org.jboss.set.pull.processor.Evaluator;
import org.jboss.set.pull.processor.EvaluatorContext;
import org.jboss.set.pull.processor.data.Attributes;
import org.jboss.set.pull.processor.data.EvaluatorData;

public class BranchEvaluator implements Evaluator {

    @Override
    public void eval(EvaluatorContext context, EvaluatorData data) {
        data.setAttributeValue(Attributes.BRANCH, context.getBranch());
    }

}

package org.jboss.pull.processor.impl.evaluator;

import org.jboss.pull.processor.Evaluator;
import org.jboss.pull.processor.EvaluatorContext;
import org.jboss.pull.processor.data.Attributes;
import org.jboss.pull.processor.data.EvaluatorData;
import org.jboss.set.aphrodite.spi.NotFoundException;

public class WritableRepositoryEvaluator implements Evaluator {

    @Override
    public void eval(EvaluatorContext context, EvaluatorData data) {
        try {
            data.setAttributeValue(Attributes.WRITE_PERMISSION, context.getAphrodite().isRepositoryLabelsModifiable(context.getRepository()));
        } catch(NotFoundException e) {
            data.setAttributeValue(Attributes.WRITE_PERMISSION, Boolean.FALSE);
        }
    }

}

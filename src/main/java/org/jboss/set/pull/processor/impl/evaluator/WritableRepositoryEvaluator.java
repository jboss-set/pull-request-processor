package org.jboss.set.pull.processor.impl.evaluator;

import org.jboss.set.aphrodite.spi.NotFoundException;
import org.jboss.set.pull.processor.Evaluator;
import org.jboss.set.pull.processor.EvaluatorContext;
import org.jboss.set.pull.processor.data.Attributes;
import org.jboss.set.pull.processor.data.EvaluatorData;

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

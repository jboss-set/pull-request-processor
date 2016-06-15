package org.jboss.pull.processor;

import org.jboss.pull.processor.data.EvaluatorData;



public interface Evaluator {

    default String name() {
        return this.getClass().getSimpleName();
    }

    void eval(EvaluatorContext context, EvaluatorData data);

}

package org.jboss.set.pull.processor;

import java.util.List;

import org.jboss.set.pull.processor.data.EvaluatorData;

public interface Action {

    void execute(ActionContext actionContext, List<EvaluatorData> data);

}

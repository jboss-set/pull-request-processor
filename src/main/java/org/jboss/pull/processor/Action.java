package org.jboss.pull.processor;

import java.util.List;

import org.jboss.pull.processor.data.EvaluatorData;

public interface Action {

    void execute(ActionContext actionContext, List<EvaluatorData> data);

}

package org.jboss.set.pull.processor.impl.action;

import java.io.File;
import java.util.List;

import org.jboss.set.pull.processor.Action;
import org.jboss.set.pull.processor.ActionContext;
import org.jboss.set.pull.processor.ProcessorPhase;
import org.jboss.set.pull.processor.data.EvaluatorData;

/**
 * Action which produces report if proper flag has been set.
 *
 * @author baranowb
 *
 */
public class ReportAction implements Action {

    @Override
    public void execute(ActionContext actionContext, List<EvaluatorData> data) {
        final File ROOT = actionContext.getRoot();
    }

    @Override
    public boolean support(ProcessorPhase processorPhase) {
        return ProcessorPhase.OPEN == processorPhase;
    }

}

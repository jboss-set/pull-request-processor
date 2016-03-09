package org.jboss.pull.processor;

import java.util.List;

import org.jboss.pull.processor.data.ProcessorData;

public interface Action {

	void execute(ActionContext actionContext, List<ProcessorData> data);
	
}

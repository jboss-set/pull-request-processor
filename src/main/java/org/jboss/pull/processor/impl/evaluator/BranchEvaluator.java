package org.jboss.pull.processor.impl.evaluator;

import java.util.Map;

import org.jboss.pull.processor.Evaluator;
import org.jboss.pull.processor.EvaluatorContext;

public class BranchEvaluator implements Evaluator {

	@Override
	public String name() {
		return "Branch Evaluator";
	}

	@Override
	public void eval(EvaluatorContext context, Map<String, Object> data) {
		data.put("branch", context.getBranch());
	}

}

package org.jboss.pull.processor.impl.evaluator;

import java.util.Map;

import org.jboss.pull.processor.Evaluator;
import org.jboss.pull.processor.EvaluatorContext;
import org.jboss.pull.processor.data.LinkData;
import org.jboss.set.aphrodite.domain.Patch;

public class PullRequestEvaluator implements Evaluator {

	@Override
	public String name() {
		return "Pull Request Evaluator";
	}
	
	@Override
	public void eval(EvaluatorContext context, Map<String, Object> data) {
		Patch patch = context.getPatch();
		data.put("pullRequest", new LinkData(patch.getId(), patch.getURL()));

	}

}

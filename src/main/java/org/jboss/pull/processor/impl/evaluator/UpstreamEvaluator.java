package org.jboss.pull.processor.impl.evaluator;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.jboss.pull.processor.Evaluator;
import org.jboss.pull.processor.EvaluatorContext;
import org.jboss.set.aphrodite.domain.Patch;

public class UpstreamEvaluator implements Evaluator {
	
	private Pattern UPSTREAM_NOT_REQUIRED = Pattern.compile(".*no.*upstream.*required.*", Pattern.CASE_INSENSITIVE);
	
	@Override
	public String name() {
		return "Upstream Evaluator";
	}

	@Override
	public void eval(EvaluatorContext context, Map<String, Object> data) {
		Patch patch = context.getPatch();
		List<Patch> related = context.getRelated();
		
		if(!UPSTREAM_NOT_REQUIRED.matcher(patch.getBody()).find()) {
			if(!related.isEmpty()) {
				data.put("messages", "missing upstream issue link");		
			} 
		}
	}
}

package org.jboss.pull.processor.impl.evaluator;

import java.util.Map;

import org.jboss.pull.processor.Evaluator;
import org.jboss.pull.processor.EvaluatorContext;
import org.jboss.pull.processor.data.LinkData;
import org.jboss.set.aphrodite.domain.Patch;
import org.jboss.set.aphrodite.domain.Repository;
import org.jboss.set.aphrodite.spi.StreamService;

public class RepositoryEvaluator implements Evaluator {

	@Override
	public String name() {
		return "Repository evaluator";
	}

	@Override
	public void eval(EvaluatorContext context, Map<String, Object> data) {
		Repository repository = context.getRepository();
		StreamService streamService = context.getStreamService();
		Patch patch = context.getPatch();
		String componentName =  streamService.findComponentNameBy(patch.getRepository(), patch.getCodebase());
		data.put("repository", new LinkData(componentName, repository.getURL()));
	}

}

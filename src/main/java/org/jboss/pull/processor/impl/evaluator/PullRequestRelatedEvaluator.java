package org.jboss.pull.processor.impl.evaluator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jboss.pull.processor.Evaluator;
import org.jboss.pull.processor.EvaluatorContext;
import org.jboss.pull.processor.data.PullRequestData;
import org.jboss.set.aphrodite.domain.Patch;
import org.jboss.set.aphrodite.domain.Stream;
import org.jboss.set.aphrodite.spi.StreamService;

public class PullRequestRelatedEvaluator implements Evaluator {

	@Override
	public String name() {
		return "Pull Request Related Evaluator";
	}

	@Override
	public void eval(EvaluatorContext context, Map<String, Object> data) {
		List<Patch> relatedPatches = context.getRelated();
		StreamService service = context.getStreamService();

		List<PullRequestData> links = new ArrayList<>();
		for(Patch patch : relatedPatches) {
			List<Stream> streams = service.findStreamsBy(patch.getRepository(), patch.getCodebase());
			List<String> streamsStr =  streams.stream().map(e -> e.getName()).collect(Collectors.toList());
			
			links.add(new PullRequestData(patch.getId(), streamsStr, patch.getURL()));
		}
		
		
		data.put("pullRequestsRelated", links);

	}

}

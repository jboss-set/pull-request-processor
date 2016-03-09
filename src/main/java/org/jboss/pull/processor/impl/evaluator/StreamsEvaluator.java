package org.jboss.pull.processor.impl.evaluator;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jboss.pull.processor.Evaluator;
import org.jboss.pull.processor.EvaluatorContext;
import org.jboss.set.aphrodite.domain.Patch;
import org.jboss.set.aphrodite.domain.Stream;
import org.jboss.set.aphrodite.spi.StreamService;

public class StreamsEvaluator implements Evaluator {

	@Override
	public String name() {
		return "Stream Match Evaluator";
	}
	
	@Override
	public void eval(EvaluatorContext context, Map<String, Object> data) {

		StreamService service = context.getStreamService();
		Patch patch = context.getPatch();
		
		List<Stream> stream = service.findStreamsBy(patch.getRepository(), patch.getCodebase());
		List<String> streamsStr = stream.stream().map(e -> e.getName()).collect(Collectors.toList());
		data.put("streams", streamsStr);

	}

}

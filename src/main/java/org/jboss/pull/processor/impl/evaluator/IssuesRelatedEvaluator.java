package org.jboss.pull.processor.impl.evaluator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jboss.pull.processor.Evaluator;
import org.jboss.pull.processor.EvaluatorContext;
import org.jboss.pull.processor.data.IssueData;
import org.jboss.set.aphrodite.domain.Issue;
import org.jboss.set.aphrodite.domain.Patch;
import org.jboss.set.aphrodite.domain.Stream;
import org.jboss.set.aphrodite.spi.StreamService;

public class IssuesRelatedEvaluator implements Evaluator {

	@Override
	public String name() {
		return "Issues Related evaluator";
	}

	@Override
	public void eval(EvaluatorContext context, Map<String, Object> data) {
		List<Issue> issues = context.getIssues();
		Map<String, List<String>> issueStream = new HashMap<>();
				
		for(Issue issue : issues) {
			List<String> streams = Util.getStreams(issue);
			issueStream.put(issue.getTrackerId().get(), streams);
		}
		
	    StreamService service = context.getStreamService();
	    Patch patch = context.getPatch();
	    List<String> streams = service.findStreamsBy(patch.getRepository(), patch.getCodebase()).stream().map(e -> e.getName()).collect(Collectors.toList());
		
		data.put("issuesRelated", issues.stream()
		    .filter(e -> {
		        List<String> intersect = new ArrayList<>(streams);
                intersect.retainAll(issueStream.get(e.getTrackerId().get()));
                return !intersect.isEmpty();
		     })
			.map(e -> new IssueData(e.getTrackerId().get(), issueStream.get(e.getTrackerId().get()), e.getURL()) )
			.collect(Collectors.toList())
		);
		
        data.put("issuesOtherStreams", issues.stream()
              .filter(e -> {
                  List<String> intersect = new ArrayList<>(streams);
                  intersect.retainAll(issueStream.get(e.getTrackerId().get()));
                  return intersect.isEmpty();
               })
              .map(e -> new IssueData(e.getTrackerId().get(), issueStream.get(e.getTrackerId().get()), e.getURL()) )
              .collect(Collectors.toList())
        );
	}

}

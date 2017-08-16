package org.jboss.set.pull.processor.impl.evaluator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.jboss.set.aphrodite.Aphrodite;
import org.jboss.set.aphrodite.domain.Issue;
import org.jboss.set.aphrodite.domain.PullRequest;
import org.jboss.set.pull.processor.Evaluator;
import org.jboss.set.pull.processor.EvaluatorContext;
import org.jboss.set.pull.processor.Main;
import org.jboss.set.pull.processor.data.Attributes;
import org.jboss.set.pull.processor.data.EvaluatorData;
import org.jboss.set.pull.processor.data.IssueData;

public class IssuesRelatedEvaluator implements Evaluator {

    @Override
    public void eval(EvaluatorContext context, EvaluatorData data) {
        try {
            List<Issue> issues = context.getIssues();
            Map<String, List<String>> issueStream = new HashMap<>();

            for (Issue issue : issues) {
                List<String> streams = Util.getStreams(issue);
                issueStream.put(issue.getTrackerId().get(), streams);
            }

            Aphrodite aphrodite = context.getAphrodite();
            PullRequest pullRequest = context.getPullRequest();
            List<String> streams = aphrodite
                    .getStreamsBy(pullRequest.getRepository().getURL().toURI(), pullRequest.getCodebase()).stream()
                    .map(e -> e.getName()).collect(Collectors.toList());

            data.setAttributeValue(Attributes.ISSUES_RELATED, issues.stream().filter(e -> {
                List<String> intersect = new ArrayList<>(streams);
                intersect.retainAll(issueStream.get(e.getTrackerId().get()));
                return !intersect.isEmpty();
            }).map(e -> new IssueData(e.getTrackerId().get(), issueStream.get(e.getTrackerId().get()), e.getURL()))
                    .collect(Collectors.toList()));

            data.setAttributeValue(Attributes.ISSUES_OTHER_STREAMS, issues.stream().filter(e -> {
                List<String> intersect = new ArrayList<>(streams);
                intersect.retainAll(issueStream.get(e.getTrackerId().get()));
                return intersect.isEmpty();
            }).map(e -> new IssueData(e.getTrackerId().get(), issueStream.get(e.getTrackerId().get()), e.getURL()))
                    .collect(Collectors.toList()));
        } catch (Exception e) {
            Main.logger.log(Level.SEVERE, "Something went wrong", e);
        }
    }

}

package org.jboss.pull.processor.impl.evaluator;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.jboss.pull.processor.Evaluator;
import org.jboss.pull.processor.EvaluatorContext;
import org.jboss.pull.processor.Main;
import org.jboss.pull.processor.data.Attributes;
import org.jboss.pull.processor.data.EvaluatorData;
import org.jboss.set.aphrodite.Aphrodite;
import org.jboss.set.aphrodite.domain.Issue;
import org.jboss.set.aphrodite.domain.PullRequest;
import org.jboss.set.aphrodite.domain.Stream;

public class StreamsEvaluator implements Evaluator {

    @Override
    public void eval(EvaluatorContext context, EvaluatorData data) {
        try {
            Aphrodite aphrodite = context.getAphrodite();
            List<Issue> issues = context.getIssues();

            List<String> issueStreams = new ArrayList<>();
            for (Issue issue : issues) {
                List<String> tmp = Util.getStreams(issue);
                tmp.removeAll(issueStreams);
                if (!tmp.isEmpty()) {
                    issueStreams.addAll(tmp);
                }
            }
            issueStreams.retainAll(context.getAllowedStreams());
            PullRequest pullRequest = context.getPullRequest();

            List<Stream> stream = aphrodite.getStreamsBy(pullRequest.getRepository().getURL().toURI(),
                    pullRequest.getCodebase());
            List<String> streamsStr = stream.stream().map(e -> e.getName()).collect(Collectors.toList());
            issueStreams.removeAll(streamsStr);
            streamsStr.addAll(issueStreams);
            data.setAttributeValue(Attributes.STREAMS, streamsStr);
        } catch (Exception e) {
            Main.logger.log(Level.SEVERE, "Something went wrong", e);
        }
    }

}

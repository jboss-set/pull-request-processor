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
import org.jboss.pull.processor.data.PullRequestData;
import org.jboss.set.aphrodite.Aphrodite;
import org.jboss.set.aphrodite.domain.PullRequest;
import org.jboss.set.aphrodite.domain.Stream;

public class PullRequestRelatedEvaluator implements Evaluator {

    @Override
    public void eval(EvaluatorContext context, EvaluatorData data) {
        try {
            List<PullRequest> relatedPatches = context.getRelated();
            Aphrodite aphrodite = context.getAphrodite();

            List<PullRequestData> links = new ArrayList<>();
            for (PullRequest pullRequest : relatedPatches) {
                List<Stream> streams = aphrodite.getStreamsBy(pullRequest.getRepository().getURL().toURI(),
                        pullRequest.getCodebase());
                List<String> streamsStr = streams.stream().map(e -> e.getName()).collect(Collectors.toList());

                links.add(new PullRequestData(pullRequest.getId(), streamsStr, pullRequest.getURL()));
            }

            data.setAttributeValue(Attributes.PULL_REQUEST_RELATED, links);
        } catch (Exception e) {
            Main.logger.log(Level.SEVERE, "Something went wrong", e);
        }
    }

}

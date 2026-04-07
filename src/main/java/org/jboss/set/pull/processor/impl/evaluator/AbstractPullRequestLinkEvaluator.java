package org.jboss.set.pull.processor.impl.evaluator;

import java.util.Arrays;

import org.jboss.set.aphrodite.domain.PullRequest;
import org.jboss.set.aphrodite.spi.NotFoundException;
import org.jboss.set.pull.processor.Evaluator;
import org.jboss.set.pull.processor.EvaluatorContext;
import org.jboss.set.pull.processor.StreamComponentDefinition;
import org.jboss.set.pull.processor.StreamDefinition;
import org.jboss.set.pull.processor.data.PullRequestData;
import org.jboss.set.pull.processor.impl.evaluator.util.StreamDefinitionUtil;

public abstract class AbstractPullRequestLinkEvaluator implements Evaluator {

    protected StreamComponentDefinition determineUpstreamStreamComponentDefinition(final EvaluatorContext context) throws NotFoundException {
        StreamComponentDefinition assumedDownstreamToMatch = context.getStreamComponentDefinition();
        if(assumedDownstreamToMatch.getStreamDefinition().getStream().getUpstream() == null)
            return null;
        StreamDefinition upstreamStreamDef = new StreamDefinition(assumedDownstreamToMatch.getStreamDefinition().getStream().getUpstream().getName(), assumedDownstreamToMatch.getName());
        StreamDefinitionUtil.matchStreams(context.getAphrodite(), Arrays.asList(upstreamStreamDef));
        return upstreamStreamDef.getStreamComponents().get(0);
    }

    protected PullRequestData convert(final PullRequest pullRequest, StreamComponentDefinition streamComponentDefinition) {
        return new PullRequestData(pullRequest, streamComponentDefinition);
    }
}

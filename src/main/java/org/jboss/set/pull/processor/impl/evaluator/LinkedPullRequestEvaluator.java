/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.set.pull.processor.impl.evaluator;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

import org.jboss.set.aphrodite.domain.PullRequest;
import org.jboss.set.aphrodite.spi.NotFoundException;
import org.jboss.set.pull.processor.Evaluator;
import org.jboss.set.pull.processor.EvaluatorContext;
import org.jboss.set.pull.processor.StreamComponentDefinition;
import org.jboss.set.pull.processor.StreamDefinition;
import org.jboss.set.pull.processor.data.Attributes;
import org.jboss.set.pull.processor.data.EvaluatorData;
import org.jboss.set.pull.processor.data.IssueData;
import org.jboss.set.pull.processor.data.PullRequestData;
import org.jboss.set.pull.processor.impl.evaluator.util.StreamDefinitionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LinkedPullRequestEvaluator implements Evaluator {

    private static final Logger LOG = LoggerFactory.getLogger(LinkedPullRequestEvaluator.class);

    @Override
    public void eval(EvaluatorContext context, EvaluatorData data) {
        try {
            PullRequestData currentPullRequestData = convert(context.getPullRequest(), context.getStreamComponentDefinition());
            data.setAttributeValue(Attributes.PULL_REQUEST_CURRENT, currentPullRequestData);

            URI upstreamPullRequestURL = context.getPullRequest().findUpstreamPullRequestURI();
            PullRequest upstreamPullRequest = upstreamPullRequestURL != null
                    ? context.getAphrodite().getPullRequest(upstreamPullRequestURL)
                    : null;
            PullRequestData upstreamPullRequestData = convert(upstreamPullRequest, determineUpstreamStreamComponentDefinition(context));
            IssueData upstreamIssue = data.getAttributeValue(Attributes.ISSUE_UPSTREAM);
            if (!upstreamIssue.isRequired() || !currentPullRequestData.getPullRequest().isUpstreamPrRequired())
                upstreamPullRequestData.notRequired();
            data.setAttributeValue(Attributes.PULL_REQUEST_UPSTREAM, upstreamPullRequestData);
        } catch (URISyntaxException | NotFoundException e) {
            LOG.error("Failed during evaluation of linked pull request", e);
        }
    }

    protected StreamComponentDefinition determineUpstreamStreamComponentDefinition(final EvaluatorContext context) throws NotFoundException {
        //check if we have stream comp, stream and if there is upstream
        //TODO: XXX no upstream == violation?
        StreamComponentDefinition assumedDownstreamToMatch = context.getStreamComponentDefinition();
        if(assumedDownstreamToMatch.getStreamDefinition().getStream().getUpstream() == null)
            return null;
        StreamDefinition upstreamStreamDef = new StreamDefinition(assumedDownstreamToMatch.getStreamDefinition().getStream().getUpstream().getName(), assumedDownstreamToMatch.getName());
        StreamDefinitionUtil.matchStreams(context.getAphrodite(), Arrays.asList(upstreamStreamDef));
        return upstreamStreamDef.getStreamComponents().get(0);
    }

    protected PullRequestData convert(final PullRequest pullRequest, StreamComponentDefinition streamComponentDefinition) {
        // simple for now
        return new PullRequestData(pullRequest, streamComponentDefinition);
    }

}

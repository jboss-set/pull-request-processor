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

import java.net.MalformedURLException;
import java.net.URL;

import org.jboss.set.aphrodite.domain.PullRequest;
import org.jboss.set.aphrodite.spi.NotFoundException;
import org.jboss.set.pull.processor.Evaluator;
import org.jboss.set.pull.processor.EvaluatorContext;
import org.jboss.set.pull.processor.ProcessorPhase;
import org.jboss.set.pull.processor.StreamComponentDefinition;
import org.jboss.set.pull.processor.data.EvaluatorData;
import org.jboss.set.pull.processor.data.IssueData;
import org.jboss.set.pull.processor.data.PullRequestData;

public class LinkedPullRequestEvaluator implements Evaluator {

    @Override
    public void eval(EvaluatorContext context, EvaluatorData data) {
        try {
            final PullRequestData currentPullRequestData = convert(context.getPullRequest(),
                    context.getStreamComponentDefinition());
            data.setAttributeValue(EvaluatorData.Attributes.PULL_REQUEST_CURRENT, currentPullRequestData);
            // TOO:XXX change this to PullRequest getUpstream() ?
            URL upstreamPullRequestURL = context.getPullRequest().findUpstreamPullRequestURL();
            final PullRequest upstreamPullRequest = upstreamPullRequestURL != null
                    ? context.getAphrodite().getPullRequest(upstreamPullRequestURL)
                    : null;
            final PullRequestData upstreamPullRequestData = convert(upstreamPullRequest, null); // TODO: check if we can fetch
                                                                                                // StreamDef
                                                                                                // for this?
            final IssueData upstreamIssue = data.getAttributeValue(EvaluatorData.Attributes.ISSUE_UPSTREAM);
            if (!upstreamIssue.isRequired())
                upstreamPullRequestData.notRequiered();
            data.setAttributeValue(EvaluatorData.Attributes.PULL_REQUEST_UPSTREAM, upstreamPullRequestData);
        } catch (MalformedURLException | NotFoundException e) {
            // TODO: XXX remove this in favor of proper reporting
            e.printStackTrace();
        }
    }

    protected PullRequestData convert(final PullRequest pullRequest,
            final StreamComponentDefinition streamComponentDefinition) {
        // simple for now
        return new PullRequestData(pullRequest, streamComponentDefinition);
    }

    @Override
    public boolean support(ProcessorPhase processorPhase) {
        if (processorPhase == ProcessorPhase.OPEN) {
            return true;
        }
        return false;
    }

}

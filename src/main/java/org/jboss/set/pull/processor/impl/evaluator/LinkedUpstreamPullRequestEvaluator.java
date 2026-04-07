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
import java.util.List;

import org.jboss.set.aphrodite.domain.PullRequest;
import org.jboss.set.aphrodite.spi.NotFoundException;
import org.jboss.set.pull.processor.EvaluatorContext;
import org.jboss.set.pull.processor.data.Attribute;
import org.jboss.set.pull.processor.data.Attributes;
import org.jboss.set.pull.processor.data.EvaluatorData;
import org.jboss.set.pull.processor.data.PullRequestData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LinkedUpstreamPullRequestEvaluator extends AbstractPullRequestLinkEvaluator {

    private static final Logger LOG = LoggerFactory.getLogger(LinkedUpstreamPullRequestEvaluator.class);

    @Override
    public void eval(EvaluatorContext context, EvaluatorData data) {
        try {
            URI upstreamPullRequestURL = context.getPullRequest().findUpstreamPullRequestURI();
            if (upstreamPullRequestURL == null) {
                LOG.info("did not find any upstream pull request for {}", context.getPullRequest().getURI());
                return;
            }
            PullRequest upstreamPullRequest = context.getAphrodite().getPullRequest(upstreamPullRequestURL);
            PullRequestData upstreamPullRequestData = convert(upstreamPullRequest, determineUpstreamStreamComponentDefinition(context));
            data.setAttributeValue(Attributes.PULL_REQUEST_UPSTREAM, upstreamPullRequestData);
        } catch (URISyntaxException | NotFoundException e) {
            LOG.error("Failed during evaluation of linked pull request", e);
        }
    }

    @Override
    public List<Attribute<?>> getProducedAttributes() {
        return List.of(Attributes.PULL_REQUEST_UPSTREAM);
    }

}

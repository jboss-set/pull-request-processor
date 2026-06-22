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
import org.jboss.set.pull.processor.data.EvaluatorReportEntry;
import org.jboss.set.pull.processor.impl.evaluator.util.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LinkedUpstreamPullRequestEvaluator extends AbstractPullRequestLinkEvaluator {

    private static final Logger LOG = LoggerFactory.getLogger(LinkedUpstreamPullRequestEvaluator.class);
    private static final String EVAL = LogUtil.pad("LinkedUpstreamPR");

    @Override
    public void eval(EvaluatorContext context, EvaluatorData data) {
        String pr = LogUtil.prRef(context.getPullRequest().getURI());
        try {
            URI upstreamPullRequestURL = context.getPullRequest().findUpstreamPullRequestURI();
            if (upstreamPullRequestURL == null) {
                LOG.info("{} | {} | no upstream PR found", pr, EVAL);
                return;
            }
            PullRequest upstreamPullRequest = context.getAphrodite().getPullRequest(upstreamPullRequestURL);
            PullRequestData upstreamPullRequestData = convert(upstreamPullRequest, determineUpstreamStreamComponentDefinition(context));
            data.setAttributeValue(Attributes.PULL_REQUEST_UPSTREAM, upstreamPullRequestData);
            String upstreamRef = LogUtil.prRef(upstreamPullRequestURL);
            EvaluatorReportEntry entry = new EvaluatorReportEntry("LinkedUpstreamPR");
            entry.addField("upstream", upstreamRef, "computed");
            entry.addField("merged", String.valueOf(upstreamPullRequest.isMerged()), "read");
            EvaluatorReportEntry.addTo(data, entry);
            LOG.info("{} | {} | upstream={} | merged={}", pr, EVAL, upstreamRef, upstreamPullRequest.isMerged());
        } catch (URISyntaxException | NotFoundException e) {
            LOG.error("{} | {} | failed to evaluate upstream PR", pr, EVAL, e);
        }
    }

    @Override
    public List<Attribute<?>> getProducedAttributes() {
        return List.of(Attributes.PULL_REQUEST_UPSTREAM);
    }

}

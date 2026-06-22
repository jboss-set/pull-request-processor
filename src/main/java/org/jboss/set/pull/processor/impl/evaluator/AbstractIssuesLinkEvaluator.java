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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.jboss.set.aphrodite.domain.Issue;
import org.jboss.set.aphrodite.domain.PullRequest;
import org.jboss.set.aphrodite.spi.NotFoundException;
import org.jboss.set.pull.processor.Evaluator;
import org.jboss.set.pull.processor.EvaluatorContext;
import org.jboss.set.pull.processor.data.EvaluatorData;
import org.jboss.set.pull.processor.data.EvaluatorReportEntry;
import org.jboss.set.pull.processor.data.IssueData;
import org.jboss.set.pull.processor.impl.evaluator.util.IssueStreamLabelsUtil;
import org.jboss.set.pull.processor.impl.evaluator.util.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simply retrieve issues from PR and process so other evaluators or action items can do some magic. This requires aphro to have
 * mentioned ops.
 *
 * @author baranowb
 *
 */
public abstract class AbstractIssuesLinkEvaluator implements Evaluator {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractIssuesLinkEvaluator.class);

    protected abstract String evaluatorLabel();

    protected abstract String producedAttributeName();

    @Override
    public void eval(EvaluatorContext context, EvaluatorData data) throws Exception {
        String pr = LogUtil.prRef(context.getPullRequest().getURI());
        try {
            List<URI> issuesURI = findIssueURI(context.getPullRequest());
            if (issuesURI.isEmpty()) {
                EvaluatorReportEntry entry = new EvaluatorReportEntry(evaluatorLabel());
                entry.addField(producedAttributeName(), "no links found", "failed");
                EvaluatorReportEntry.addTo(data, entry);
                LOG.info("{} | {} | no links found", pr, LogUtil.pad(evaluatorLabel()));
                return;
            }

            LOG.info("{} | {} | links={}", pr, LogUtil.pad(evaluatorLabel()), issuesURI);
            List<IssueData> issuesData = new ArrayList<>();
            for (URI issueURL : issuesURI) {
                // The Jira rate limit currently imposed is 1 call per 2 seconds per node per user.
                TimeUnit.SECONDS.sleep(2);
                Issue issue = findIssue(context, issueURL);
                IssueData issueData = convert(issue);
                if (issue == null && !context.getPullRequest().isIssueRequired()) {
                    issueData.notRequired();
                }
                issuesData.add(issueData);
            }
            setIssueData(data, issuesData);

            EvaluatorReportEntry entry = new EvaluatorReportEntry(evaluatorLabel());
            for (IssueData id : issuesData) {
                if (id.isDefined()) {
                    String issueRef = id.getIssue().getURI().getPath().replaceFirst(".*/browse/", "");
                    entry.addField(producedAttributeName(), issueRef, "computed");
                } else {
                    entry.addField(producedAttributeName(), "not resolved", "failed");
                }
            }
            EvaluatorReportEntry.addTo(data, entry);

        } catch (URISyntaxException e) {
            EvaluatorReportEntry entry = new EvaluatorReportEntry(evaluatorLabel());
            entry.addField(producedAttributeName(), e.getMessage(), "failed");
            EvaluatorReportEntry.addTo(data, entry);
            LOG.error("{} | {} | error: {}", pr, LogUtil.pad(evaluatorLabel()), e.getMessage(), e);
        }
    }

    private Issue findIssue(EvaluatorContext context, URI issueURL) {
        try {
            return context.getAphrodite().getIssue(issueURL);
        } catch (NotFoundException e) {
            LOG.error("find issue uri", e);
            return null;
        }
    }

    public abstract List<URI> findIssueURI(PullRequest pullRequest) throws URISyntaxException;

    public abstract void setIssueData(EvaluatorData data, List<IssueData> issueData);

    protected IssueData convert(final Issue issue) {
        if (issue == null) {
            return new IssueData();// default, we should return more than null;
        }
        final IssueData issueData = new IssueData(issue, IssueStreamLabelsUtil.getStreams(issue));
        return issueData;
    }

}

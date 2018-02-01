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
import java.util.ArrayList;
//import java.util.ArrayList;
import java.util.List;

import org.jboss.set.aphrodite.domain.Issue;
import org.jboss.set.aphrodite.spi.NotFoundException;
import org.jboss.set.pull.processor.Evaluator;
import org.jboss.set.pull.processor.EvaluatorContext;
import org.jboss.set.pull.processor.ProcessorPhase;
import org.jboss.set.pull.processor.data.EvaluatorData;
import org.jboss.set.pull.processor.data.IssueData;

/**
 * Simply retrieve issues from PR and process so other evaluators or action items can do some magic. This requires aphro to have
 * mentioned ops.
 *
 * @author baranowb
 *
 */
public class LinkedIssuesEvaluator implements Evaluator {

    @Override
    public void eval(EvaluatorContext context, EvaluatorData data) {
        // TODO: handle exception on get op?
        // TODO: XXX move those to PullRequest ?
        try {
            URL issueURL = context.getPullRequest().findIssueURL();
            final Issue currentIssue = issueURL != null ? context.getAphrodite().getIssue(issueURL) : null;
            URL upstreamIssueURL = context.getPullRequest().findUpstreamIssueURL();
            final Issue upstreamIssue = upstreamIssueURL != null ? context.getAphrodite().getIssue(upstreamIssueURL) : null;
            final List<Issue> relatedIssues = context.getAphrodite().getIssues(context.getPullRequest().findRelatedIssuesURL());

            final IssueData currentIssueData = convert(currentIssue);
            final IssueData upstreamIssueData = convert(upstreamIssue);
            final List<IssueData> relatedIssiesData = new ArrayList<>();
            if (relatedIssues != null) {
                for (Issue relatedIssue : relatedIssues) {
                    relatedIssiesData.add(convert(relatedIssue));
                }
            }

            // TODO: XXX move those to PullRequest ?
            if (upstreamIssue == null && context.getPullRequest().isUpstreamRequired()) {
                upstreamIssueData.notRequired();
            }
            // TODO: XXX what if no upstream required but its there?
            data.setAttributeValue(EvaluatorData.Attributes.ISSUE_CURRENT, currentIssueData);
            data.setAttributeValue(EvaluatorData.Attributes.ISSUE_UPSTREAM, upstreamIssueData);
            data.setAttributeValue(EvaluatorData.Attributes.ISSUES_RELATED, relatedIssiesData);
        } catch (MalformedURLException | NotFoundException e) {
            // TODO: XXX remove this in favor of proper reporting
            e.printStackTrace();
        }
    }

    protected IssueData convert(final Issue issue) {
        if (issue == null) {
            return new IssueData();// default, we should return more than null;
        }
        final IssueData issueData = new IssueData(issue, Util.getStreams(issue));
        return issueData;
    }

    @Override
    public boolean support(ProcessorPhase processorPhase) {
        return true; // if its in meta, we can use it at any stage?
    }

}

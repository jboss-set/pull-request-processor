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
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.jboss.set.aphrodite.domain.Issue;
import org.jboss.set.aphrodite.spi.NotFoundException;
import org.jboss.set.pull.processor.Evaluator;
import org.jboss.set.pull.processor.EvaluatorContext;
import org.jboss.set.pull.processor.ProcessorPhase;
import org.jboss.set.pull.processor.data.EvaluatorData;
import org.jboss.set.pull.processor.data.IssueData;
import org.jboss.set.pull.processor.impl.evaluator.util.IssueStreamLabelsUtil;

/**
 * Simply retrieve issues from PR and process so other evaluators or action items can do some magic. This requires aphro to have
 * mentioned ops.
 *
 * @author baranowb
 *
 */
public class LinkedIssuesEvaluator implements Evaluator {

    @Override
    public void eval(EvaluatorContext context, EvaluatorData data) throws InterruptedException {
        // TODO: handle exception on get op?
        URL issueURL;
        Issue currentIssue = null;
        try {
            issueURL = context.getPullRequest().findIssueURL();
            if (issueURL != null) {
                // The Jira rate limit currently imposed is 1 call per 2 seconds per node per user.
                TimeUnit.SECONDS.sleep(2);
                currentIssue = context.getAphrodite().getIssue(issueURL);
            }

        } catch (MalformedURLException | NotFoundException e) {
            e.printStackTrace();
        } finally {
            final IssueData currentIssueData = convert(currentIssue);
            if (currentIssue == null && !context.getPullRequest().isIssueRequired()) {
                currentIssueData.notRequired();
            }
            data.setAttributeValue(EvaluatorData.Attributes.ISSUE_CURRENT, currentIssueData);
        }

        URL upstreamIssueURL;
        Issue upstreamIssue = null;
        try {
            upstreamIssueURL = context.getPullRequest().findUpstreamIssueURL();
            if (upstreamIssueURL != null) {
                // The Jira rate limit currently imposed is 1 call per 2 seconds per node per user.
                TimeUnit.SECONDS.sleep(2);
                upstreamIssue = context.getAphrodite().getIssue(upstreamIssueURL);
            }
        } catch (MalformedURLException | NotFoundException e) {
            e.printStackTrace();
        } finally {
            final IssueData upstreamIssueData = convert(upstreamIssue);
            // TODO: XXX move those to PullRequest ?
            if (upstreamIssue == null && !context.getPullRequest().isUpstreamRequired()) {
                upstreamIssueData.notRequired();
            }
            data.setAttributeValue(EvaluatorData.Attributes.ISSUE_UPSTREAM, upstreamIssueData);
        }

        List<Issue> relatedIssues = null;
        try {
            // The Jira rate limit currently imposed is 1 call per 2 seconds per node per user.
            TimeUnit.SECONDS.sleep(2);
            relatedIssues = context.getAphrodite().getIssues(context.getPullRequest().findRelatedIssuesURL());
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            final List<IssueData> relatedIssiesData = new ArrayList<>();
            if (relatedIssues != null) {
                for (Issue relatedIssue : relatedIssues) {
                    relatedIssiesData.add(convert(relatedIssue));
                }
            }
            data.setAttributeValue(EvaluatorData.Attributes.ISSUES_RELATED, relatedIssiesData);
        }
    }

    protected IssueData convert(final Issue issue) {
        if (issue == null) {
            return new IssueData();// default, we should return more than null;
        }
        final IssueData issueData = new IssueData(issue, IssueStreamLabelsUtil.getStreams(issue));
        return issueData;
    }

    @Override
    public boolean support(ProcessorPhase processorPhase) {
        return true; // if its in meta, we can use it at any stage?
    }

}

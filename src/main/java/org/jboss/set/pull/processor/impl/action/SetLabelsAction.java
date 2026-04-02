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
package org.jboss.set.pull.processor.impl.action;

import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.naming.NameNotFoundException;

import org.jboss.set.aphrodite.domain.Label;
import org.jboss.set.aphrodite.domain.PullRequest;
import org.jboss.set.aphrodite.spi.NotFoundException;
import org.jboss.set.pull.processor.Action;
import org.jboss.set.pull.processor.ActionContext;
import org.jboss.set.pull.processor.data.Attributes;
import org.jboss.set.pull.processor.data.EvaluatorData;
import org.jboss.set.pull.processor.data.IssueData;
import org.jboss.set.pull.processor.data.LabelData;
import org.jboss.set.pull.processor.data.LabelItem;
import org.jboss.set.pull.processor.data.LabelItem.LabelAction;
import org.jboss.set.pull.processor.data.LabelItem.LabelSeverity;
import org.jboss.set.pull.processor.data.PullRequestData;
import org.jboss.set.pull.processor.data.ReportItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SetLabelsAction implements Action {

    private static final Logger LOG = LoggerFactory.getLogger(SetLabelsAction.class);
    private static final String Request_Changes_Comment = "According to [pull-request-review-criteria-for-merge](https://source.redhat.com/groups/public/jboss-sustaining-engineering-team/jboss_sustaining_engineering_team_wiki/pull_request_review_criteria_for_merge) document，this pull request does not satisfy all review criteria for merge. Please check the associated pull request labels to revise.";

    @Override
    public void execute(ActionContext actionContext, EvaluatorData data) {
//        try {
//        setLabels(actionContext, data);
//        } catch(Exception e) {
//            LOG.error("Error !", e);
//        }
    }

    private void setLabels(ActionContext actionContext, EvaluatorData data) throws Exception {
        // TODO: XXX cross check REMOVE list vs CURRENT list to avoid mute removal
        final PullRequestData pullRequestData = data.getAttributeValue(Attributes.PULL_REQUEST_CURRENT);
        final IssueData issueData = data.getAttributeValue(Attributes.ISSUE_CURRENT);
        final LabelData labelsData = data.getAttributeValue(Attributes.LABELS_CURRENT);
        final PullRequestData upstreamPullRequestData = data.getAttributeValue(Attributes.PULL_REQUEST_UPSTREAM);
        final IssueData upstreamIssueData = data.getAttributeValue(Attributes.ISSUE_UPSTREAM);
        final LabelData upstreamLabelsData = data.getAttributeValue(Attributes.LABELS_UPSTREAM);
        final Set<Label> currentLabels = new TreeSet<>(new LabelComparator());
        final PullRequest pullRequest = pullRequestData.getPullRequest();
        currentLabels.addAll(pullRequest.getLabels());

        final List<LabelItem<?>> addList = labelsData.getLabels(LabelAction.SET);
        final List<LabelItem<?>> removeList = labelsData.getLabels(LabelAction.REMOVE);
        // TODO: XXX make this part of super class, "AbstractConsoleReporting" or something.
        // or something more generic avavilable for whole tool/s
        final StringBuilder logBuilder = new StringBuilder();

        final URI url = pullRequest.getURI();
        final String issue = issueData.isDefined() ? issueData.getIssue().getURI().toString() : "n/a";
        final List<String> currentLabelsNames = currentLabels.stream().map(l -> l.getName()).collect(Collectors.toList());
        final List<String> addLabelsNames = addList.stream().map(l -> l.getLabel()).collect(Collectors.toList());
        final List<String> removeLabelsNames = removeList.stream().map(l -> l.getLabel()).collect(Collectors.toList());
        logBuilder.append("\n... ").append(url);
        logBuilder.append("\n   |... ").append(issue);
        logBuilder.append("\n   |... C:").append(currentLabelsNames);
        logBuilder.append("\n   |... A:").append(addLabelsNames);
        logBuilder.append("\n   |... R:").append(removeLabelsNames);

        // For the HTML report file
        ReportItem ri = new ReportItem(url.toString(), issue, currentLabelsNames, addLabelsNames, removeLabelsNames);
        ReportAction.addItemToReport(ri);

        if (upstreamPullRequestData.isDefined()) {
            final Set<Label> upstreamLabels = new TreeSet<>(new LabelComparator());
            upstreamLabels.addAll(pullRequest.getLabels());

            // just for info ?
            logBuilder.append("\n   |... Upstream ");
            logBuilder.append("\n       |... ").append((upstreamIssueData.isDefined() ? upstreamIssueData.getIssue().getURI() : "n/a"));
            logBuilder.append("\n       |... C:").append(upstreamLabels.stream().map(l -> l.getName()).collect(Collectors.toList()));
            if (upstreamIssueData.isDefined()) {
                final List<LabelItem<?>> upstreamAddList = upstreamLabelsData.getLabels(LabelAction.SET);
                final List<LabelItem<?>> upstreamRemoveList = upstreamLabelsData.getLabels(LabelAction.REMOVE);
                logBuilder.append("\n       |... A:").append(upstreamAddList.stream().map(l -> l.getLabel()).collect(Collectors.toList()));
                logBuilder.append("\n       |... R:").append(upstreamRemoveList.stream().map(l -> l.getLabel()).collect(Collectors.toList()));
            }
        }

        boolean requestChanges = addList.stream().filter(l -> l.getSeverity() == LabelSeverity.BAD).findAny().isPresent();

        if (requestChanges) {
            logBuilder.append(("\n|... Request changes on Pull Request."));
        } else {
            logBuilder.append(("\n|... Approve on Pull Request."));
        }

        if (!actionContext.isWritePermitted() || !actionContext.isWritePermitedOn(pullRequest)) {
            logBuilder.append("\n   |... Write: <<Skipped>>");
            LOG.info(logBuilder.toString());
            return;
        }

        LOG.info(logBuilder.toString());
        List<Label> actionItems = convertLabels(actionContext, removeList, pullRequest);
        for (Label l : actionItems.stream().filter(currentLabels::contains).toList()) {
            // remove only if it is present, else skip
            pullRequest.removeLabel(l);
        }
        currentLabels.removeAll(actionItems); // remove also from currentLabels, as we need to set current labels in below.
        actionItems = convertLabels(actionContext, addList, pullRequest);
        // allow to reset developer's label, as 'hold', 'bug', otherwise they would be removed by setLabelsToPullRequest()
        for (Label l : actionItems) {
            // retain only those are not present, so we don't try to set them twice
            if (currentLabels.contains(l)) {
                currentLabels.remove(l);
            }
        }
        actionItems.addAll(currentLabels);
        actionItems.forEach(label -> {
            try {
                pullRequest.addLabel(label);
            } catch(NameNotFoundException e) {
                LOG.error("label not found", e);
            }
        });

        // update pull request review
        // SET-464 Disable pull request review action by default, turn on by option "-r true".
        if (actionContext.isReviewPermitted()) {
            if (requestChanges) {
                pullRequest.requestChangesOnPullRequest(Request_Changes_Comment);
            } else {
                pullRequest.approveOnPullRequest();
            }
        }
    }

    // TODO: XXX change those once aphrodite has been updated. #142
    private List<Label> convertLabels(ActionContext actionContext, List<LabelItem<?>> labelItems, PullRequest pullRequest) throws NotFoundException {
        List<Label> repoLabels = actionContext.getAphrodite().getLabelsFromRepository(pullRequest.getRepository());
        // this could be done with elaborate and confusing one line lambda
        final Set<String> names = labelItems.stream().map(li -> li.getLabel().toString()).collect(Collectors.toSet());
        repoLabels = repoLabels.stream().filter(rl -> {
            if (names.contains(rl.getName())) {
                names.remove(rl.getName());
                return true;
            } else {
                return false;
            }
        }).collect(Collectors.toList());
        if (names.size() != 0) {
            LOG.warn("Failed to convert {} into proper repo label.", names);
        }
        return repoLabels;
    }

    private static class LabelComparator implements Comparator<Label> {
        // Label comparator - to have it neat and possible, if Git retain order, to have it always the same way?
        @Override
        public int compare(Label o1, Label o2) {
            if (o1 == null) {
                return -1;
            }
            if (o2 == null) {
                return 1;
            }
            if (o1.equals(o2)) {
                return 0;
            }
            return o1.getName().compareTo(o2.getName());
        }
    }
}

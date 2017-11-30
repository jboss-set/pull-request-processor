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

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.jboss.set.aphrodite.domain.Label;
import org.jboss.set.aphrodite.domain.PullRequest;
import org.jboss.set.aphrodite.spi.NotFoundException;
import org.jboss.set.pull.processor.Action;
import org.jboss.set.pull.processor.ActionContext;
import org.jboss.set.pull.processor.ProcessorPhase;
import org.jboss.set.pull.processor.data.EvaluatorData;
import org.jboss.set.pull.processor.data.IssueData;
import org.jboss.set.pull.processor.data.LabelData;
import org.jboss.set.pull.processor.data.LabelItem;
import org.jboss.set.pull.processor.data.LabelItem.LabelAction;
import org.jboss.set.pull.processor.data.PullRequestData;

public class SetLabelsAction implements Action {

    private static final Logger LOG = Logger.getLogger(SetLabelsAction.class.getName());

    @Override
    public void execute(final ActionContext actionContext, final List<EvaluatorData> data) {
        try {
            actionContext.getExecutors().invokeAll(
                    data.stream().map(e -> new EvaluatorProcessingTask(actionContext, e)).collect(Collectors.toList()));
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean support(ProcessorPhase processorPhase) {
        if (processorPhase == ProcessorPhase.OPEN) {// true only for OPEN, in close we just post process?
            return true;
        } else {
            return false;
        }
    }

    // we can process all PRs concurently, no connection between them should exist
    private class EvaluatorProcessingTask implements Callable<Void> {

        private final EvaluatorData data;
        private final ActionContext actionContext;

        public EvaluatorProcessingTask(final ActionContext actionContext, final EvaluatorData data) {
            super();
            this.data = data;
            this.actionContext = actionContext;
        }

        @Override
        public Void call() throws Exception {
            //TODO: XXX cross check REMOVE list vs CURRENT list to avoid mute removal
            final PullRequestData pullRequestData = data.getAttributeValue(EvaluatorData.Attributes.PULL_REQUEST_CURRENT);
            final IssueData issueData = data.getAttributeValue(EvaluatorData.Attributes.ISSUE_CURRENT);
            final LabelData labelsData = data.getAttributeValue(EvaluatorData.Attributes.LABELS_CURRENT);
            final PullRequestData upstreamPullRequestData = data
                    .getAttributeValue(EvaluatorData.Attributes.PULL_REQUEST_UPSTREAM);
            final IssueData upstreamIssueData = data.getAttributeValue(EvaluatorData.Attributes.ISSUE_UPSTREAM);
            final LabelData upstreamLabelsData = data.getAttributeValue(EvaluatorData.Attributes.LABELS_UPSTREAM);
            final Set<Label> currentLabels = new TreeSet(new LabelComparator());
            currentLabels.addAll(actionContext.getAphrodite().getLabelsFromPullRequest(pullRequestData.getPullRequest()));

            final List<LabelItem<?>> addList = labelsData.getLabels(LabelAction.SET);
            final List<LabelItem<?>> removeList = labelsData.getLabels(LabelAction.REMOVE);
            //TODO: XXX make this part of super class, "AbstractConsoleReporting" or something.
            // or something more generic avavilable for whole tool/s
            final StringBuilder logBuilder = new StringBuilder();
            logBuilder.append("\n... " + pullRequestData.getPullRequest().getURL());
            logBuilder.append("\n   |... " + (issueData.isDefined() ? issueData.getIssue().getURL() : "n/a"));
            logBuilder.append("\n   |... C:" + currentLabels.stream().map(l -> l.getName()).collect(Collectors.toList()));
            logBuilder.append("\n   |... S:" + addList.stream().map(l -> l.getLabel()).collect(Collectors.toList()));
            logBuilder.append("\n   |... R:" + removeList.stream().map(l -> l.getLabel()).collect(Collectors.toList()));

            if (upstreamPullRequestData.isDefined()) {
                final Set<Label> upstreamLabels = new TreeSet(new LabelComparator());
                upstreamLabels.addAll(
                        actionContext.getAphrodite().getLabelsFromPullRequest(upstreamPullRequestData.getPullRequest()));

                final List<LabelItem<?>> upstreamAddList = upstreamLabelsData.getLabels(LabelAction.SET);
                final List<LabelItem<?>> upstreamRemoveList = upstreamLabelsData.getLabels(LabelAction.REMOVE);
                // just for info ?
                logBuilder.append("\n   |... Upstream ");
                logBuilder.append("\n       |... " + (upstreamIssueData.isDefined() ? upstreamIssueData.getIssue().getURL() : "n/a"));
                logBuilder.append("\n       |... C:" + upstreamLabels.stream().map(l -> l.getName()).collect(Collectors.toList()));
                logBuilder.append("\n       |... S:" + upstreamAddList.stream().map(l -> l.getLabel()).collect(Collectors.toList()));
                logBuilder.append("\n       |... R:" + upstreamRemoveList.stream().map(l -> l.getLabel()).collect(Collectors.toList()));
            }
            if (!actionContext.isWritePermitted() || !actionContext.isWritePermitedOn(pullRequestData.getPullRequest())) {
                logBuilder.append("\n   |... Write: <<Skipped>>");
                LOG.log(Level.INFO, logBuilder.toString());
                return null;
            }

            LOG.log(Level.INFO, logBuilder.toString());
            List<Label> actionItems = convertLabels(removeList, pullRequestData.getPullRequest());
            for (Label l : actionItems) {
                if (currentLabels.contains(l)) {
                    // remove only if it is present, else skip
                    actionContext.getAphrodite().removeLabelFromPullRequest(pullRequestData.getPullRequest(), l.getName());
                    currentLabels.remove(l); // remove also from currentLabels, as we need to set current labels in below.
                }
            }

            actionItems = convertLabels(addList, pullRequestData.getPullRequest());
            // allow to reset developer's label, as 'hold', 'bug', otherwise they would be removed by setLabelsToPullRequest()
            for (Label l : actionItems) {
                // retain only those are not present, so we don't try to set them twice
                if (currentLabels.contains(l)) {
                    currentLabels.remove(l);
                }
            }
            actionItems.addAll(currentLabels);

            if (!actionItems.isEmpty()) {
                actionContext.getAphrodite().setLabelsToPullRequest(pullRequestData.getPullRequest(), actionItems);
            }
            return null;
        }

        // TODO: XXX change those once aphrodite has been updated. #142
        private List<Label> convertLabels(final List<LabelItem<?>> labelItems, final PullRequest pullRequest)
                throws NotFoundException {
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
                LOG.log(Level.WARNING, "Failed to convert " + names + " into proper repo label.");
            }
            return repoLabels;
        }
    }

    private static class LabelComparator implements Comparator<Label> {
        //Label comparator - to have it neat and possiblt, if Git retain order, to have it always the same way?
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

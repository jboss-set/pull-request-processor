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

import org.jboss.set.aphrodite.domain.Codebase;
import org.jboss.set.pull.processor.EvaluatorContext;
import org.jboss.set.pull.processor.data.Attributes;
import org.jboss.set.pull.processor.data.DefinedLabelItem;
import org.jboss.set.pull.processor.data.DefinedLabelItem.LabelContent;
import org.jboss.set.pull.processor.data.EvaluatorData;
import org.jboss.set.pull.processor.data.LabelData;
import org.jboss.set.pull.processor.data.LabelItem;
import org.jboss.set.pull.processor.data.PullRequestData;

/**
 * Check status of PR and if we need it.
 *
 * @author baranowb
 *
 */
public class UpstreamPullRequestLabelEvaluator extends AbstractLabelEvaluator {

    @Override
    public void eval(EvaluatorContext context, EvaluatorData data) {
        final PullRequestData upstreamPullRequestData = data.getAttributeValue(Attributes.PULL_REQUEST_UPSTREAM);
        final LabelData labelData = super.getLabelData(Attributes.LABELS_CURRENT, data);

        boolean isMismatched = isUpsreamPRMismatched(labelData, upstreamPullRequestData);

        if (upstreamPullRequestData.isDefined()) {
            labelData.addLabelItem(new DefinedLabelItem(LabelContent.Missing_upstream_PR, LabelItem.LabelAction.REMOVE,
                    LabelItem.LabelSeverity.OK));
            if (upstreamPullRequestData.isMerged() && !isMismatched) {
                labelData.addLabelItem(new DefinedLabelItem(LabelContent.Upstream_merged, LabelItem.LabelAction.SET,
                        LabelItem.LabelSeverity.OK));
            } else {
                labelData.addLabelItem(new DefinedLabelItem(LabelContent.Upstream_merged, LabelItem.LabelAction.REMOVE,
                        LabelItem.LabelSeverity.BAD));
            }
        } else {
            // not defined, check if it is required
            if (upstreamPullRequestData.isRequired()) {
                labelData.addLabelItem(new DefinedLabelItem(LabelContent.Missing_upstream_PR, LabelItem.LabelAction.SET,
                        LabelItem.LabelSeverity.BAD));
            } else {
                // not defined and not required, remove both.
                labelData.addLabelItem(new DefinedLabelItem(LabelContent.Missing_upstream_PR, LabelItem.LabelAction.REMOVE,
                        LabelItem.LabelSeverity.OK));
                labelData.addLabelItem(new DefinedLabelItem(LabelContent.Upstream_merged, LabelItem.LabelAction.REMOVE,
                        LabelItem.LabelSeverity.BAD));
            }
        }
    }

    protected boolean isUpsreamPRMismatched(final LabelData labelData, final PullRequestData pullRequestData) {
        if (!pullRequestData.isRequired() || !pullRequestData.isDefined()) {
            // it's not defined or not required, just remove and return
            LabelItem<?> li = new DefinedLabelItem(DefinedLabelItem.LabelContent.Upstream_PR_Repository_Mismatch, LabelItem.LabelAction.REMOVE, LabelItem.LabelSeverity.OK);
            labelData.addLabelItem(li);
            li = new DefinedLabelItem(DefinedLabelItem.LabelContent.Upstream_PR_Branch_Mismatch, LabelItem.LabelAction.REMOVE, LabelItem.LabelSeverity.OK);
            labelData.addLabelItem(li);
            return false;
        }

        boolean isMismatched = false;
        final URI pullRequestRepositoryURI = pullRequestData.getPullRequest().getRepository().getURI();
        final URI componentRepositoryURI =  pullRequestData.getStreamComponentDefinition().getStreamComponent().getRepositoryURI();
        if(pullRequestRepositoryURI.equals(componentRepositoryURI)) {
            LabelItem<?> li = new DefinedLabelItem(DefinedLabelItem.LabelContent.Upstream_PR_Repository_Mismatch, LabelItem.LabelAction.REMOVE, LabelItem.LabelSeverity.OK);
            labelData.addLabelItem(li);
        } else {
            LabelItem<?> li = new DefinedLabelItem(DefinedLabelItem.LabelContent.Upstream_PR_Repository_Mismatch, LabelItem.LabelAction.SET, LabelItem.LabelSeverity.BAD);
            labelData.addLabelItem(li);
            isMismatched = true;
        }

        Codebase prCodeBase = pullRequestData.getPullRequest().getCodebase();
        Codebase codeBase = pullRequestData.getStreamComponentDefinition().getStreamComponent().getCodebase();
        // This hack for upstream branch rename from "master" to "main", add an ability to check codebase like "master|main"
        if (prCodeBase.isIn(codeBase)) {
            LabelItem<?> li = new DefinedLabelItem(DefinedLabelItem.LabelContent.Upstream_PR_Branch_Mismatch, LabelItem.LabelAction.REMOVE, LabelItem.LabelSeverity.OK);
            labelData.addLabelItem(li);
        } else {
            LabelItem<?> li = new DefinedLabelItem(DefinedLabelItem.LabelContent.Upstream_PR_Branch_Mismatch, LabelItem.LabelAction.SET, LabelItem.LabelSeverity.BAD);
            labelData.addLabelItem(li);
            isMismatched = true;
        }
        return isMismatched;
    }

}

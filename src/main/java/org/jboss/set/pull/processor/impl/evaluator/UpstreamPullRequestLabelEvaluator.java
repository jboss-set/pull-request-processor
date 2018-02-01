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

import org.jboss.set.pull.processor.EvaluatorContext;
import org.jboss.set.pull.processor.ProcessorPhase;
import org.jboss.set.pull.processor.data.DefinedLabelItem;
import org.jboss.set.pull.processor.data.EvaluatorData;
import org.jboss.set.pull.processor.data.LabelData;
import org.jboss.set.pull.processor.data.LabelItem;
import org.jboss.set.pull.processor.data.PullRequestData;
import static org.jboss.set.pull.processor.data.DefinedLabelItem.LabelContent;

/**
 * Check status of PR and if we need it.
 *
 * @author baranowb
 *
 */
public class UpstreamPullRequestLabelEvaluator extends AbstractLabelEvaluator {

    @Override
    public void eval(EvaluatorContext context, EvaluatorData data) {
        final PullRequestData upstreamPullRequestData = data.getAttributeValue(EvaluatorData.Attributes.PULL_REQUEST_UPSTREAM);
        LabelData labelData = super.getLabelData(EvaluatorData.Attributes.LABELS_UPSTREAM, data);

        if (upstreamPullRequestData.isDefined()) {
            labelData.addLabelItem(new DefinedLabelItem(LabelContent.Missing_upstream_PR, LabelItem.LabelAction.REMOVE,
                    LabelItem.LabelSeverity.OK));
            if (upstreamPullRequestData.isMerged()) {
                labelData.addLabelItem(new DefinedLabelItem(LabelContent.Upstream_merged, LabelItem.LabelAction.SET,
                        LabelItem.LabelSeverity.OK));
            } else {
                // TODO: check if this can happen
                labelData.addLabelItem(new DefinedLabelItem(LabelContent.Upstream_merged, LabelItem.LabelAction.REMOVE,
                        LabelItem.LabelSeverity.BAD));
            }
        } else {
            // not defined, check if it is required
            if (upstreamPullRequestData.isRequired()) {
                labelData.addLabelItem(new DefinedLabelItem(LabelContent.Missing_upstream_PR, LabelItem.LabelAction.SET,
                        LabelItem.LabelSeverity.BAD));
            }
        }
    }

    @Override
    public boolean support(ProcessorPhase processorPhase) {
        if (processorPhase == ProcessorPhase.OPEN) {
            return true;
        }
        return false;
    }

}

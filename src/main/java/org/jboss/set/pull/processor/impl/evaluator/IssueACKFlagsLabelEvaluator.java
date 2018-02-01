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

import org.jboss.set.aphrodite.domain.FlagStatus;
import org.jboss.set.pull.processor.EvaluatorContext;
import org.jboss.set.pull.processor.ProcessorPhase;
import org.jboss.set.pull.processor.data.DefinedLabelItem;
import org.jboss.set.pull.processor.data.EvaluatorData;
import org.jboss.set.pull.processor.data.EvaluatorData.Attribute;
import org.jboss.set.pull.processor.data.IssueData;
import org.jboss.set.pull.processor.data.LabelData;
import org.jboss.set.pull.processor.data.LabelItem;
import static org.jboss.set.pull.processor.data.DefinedLabelItem.LabelContent;

/**
 * Check what ACK flags we have and set up labels accordingly(or clear).
 *
 * @author baranowb
 *
 */
public class IssueACKFlagsLabelEvaluator extends AbstractLabelEvaluator {

    @Override
    public void eval(EvaluatorContext context, EvaluatorData data) {
        processAckLabels(EvaluatorData.Attributes.ISSUE_CURRENT, EvaluatorData.Attributes.LABELS_CURRENT, data);
        processAckLabels(EvaluatorData.Attributes.ISSUE_UPSTREAM, EvaluatorData.Attributes.LABELS_UPSTREAM, data);
    }

    protected void processAckLabels(final Attribute<IssueData> issueKey, final Attribute<LabelData> labelsKey,
            final EvaluatorData data) {
        final IssueData issueToProcess = data.getAttributeValue(issueKey);
        if (issueToProcess.isDefined()) {
            LabelData labelData = super.getLabelData(labelsKey, data);
            boolean hasAllAcks = true;
            hasAllAcks = hasAllAcks & processFlagStatus(labelData, issueToProcess.getPmAckStatus(), LabelContent.Needs_pm_ack);
            hasAllAcks = hasAllAcks
                    & processFlagStatus(labelData, issueToProcess.getDevAckStatus(), LabelContent.Needs_devel_ack);
            hasAllAcks = hasAllAcks & processFlagStatus(labelData, issueToProcess.getQeAckStatus(), LabelContent.Needs_qa_ack);

            if (hasAllAcks) {
                LabelItem<?> li = new DefinedLabelItem(LabelContent.Has_All_Acks, LabelItem.LabelAction.SET,
                        LabelItem.LabelSeverity.OK);
                labelData.addLabelItem(li);
            } else {
                LabelItem<?> li = new DefinedLabelItem(LabelContent.Has_All_Acks, LabelItem.LabelAction.REMOVE,
                        LabelItem.LabelSeverity.BAD);
                labelData.addLabelItem(li);
            }
        }
    }

    protected boolean processFlagStatus(final LabelData labelData, final FlagStatus status, final LabelContent label) {
        if (status.equals(FlagStatus.ACCEPTED)) {
            LabelItem<?> li = new DefinedLabelItem(label, LabelItem.LabelAction.REMOVE, LabelItem.LabelSeverity.OK);
            labelData.addLabelItem(li);
            return true;
        } else {
            LabelItem<?> li = new DefinedLabelItem(label, LabelItem.LabelAction.SET, LabelItem.LabelSeverity.BAD);
            labelData.addLabelItem(li);
            return false;
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

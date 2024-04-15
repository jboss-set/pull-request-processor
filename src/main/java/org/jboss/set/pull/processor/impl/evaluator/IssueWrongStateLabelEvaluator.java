/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2024, Red Hat, Inc., and individual contributors
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

import org.jboss.set.aphrodite.domain.IssueStatus;
import org.jboss.set.pull.processor.EvaluatorContext;
import org.jboss.set.pull.processor.ProcessorPhase;
import org.jboss.set.pull.processor.data.DefinedLabelItem;
import org.jboss.set.pull.processor.data.DefinedLabelItem.LabelContent;
import org.jboss.set.pull.processor.data.EvaluatorData;
import org.jboss.set.pull.processor.data.EvaluatorData.Attribute;
import org.jboss.set.pull.processor.data.IssueData;
import org.jboss.set.pull.processor.data.LabelData;
import org.jboss.set.pull.processor.data.LabelItem;

/**
 * Force ticket update if not set. Closed states are handled differently.
 *
 * @author baranowb
 *
 */
public class IssueWrongStateLabelEvaluator extends AbstractLabelEvaluator {

    @Override
    public void eval(final EvaluatorContext context, final EvaluatorData data) {
        processPresenceLabel(EvaluatorData.Attributes.ISSUE_CURRENT, EvaluatorData.Attributes.LABELS_CURRENT,
                LabelContent.Corrupted_issue_wrong_state, data, context);

    }

    protected void processPresenceLabel(final Attribute<IssueData> issueKey, final Attribute<LabelData> labelsKey,
            final LabelContent expectoPatronum, final EvaluatorData data, final EvaluatorContext context) {
        LabelData labelData = super.getLabelData(labelsKey, data);
        final IssueData issueToProcess = data.getAttributeValue(issueKey);
        final IssueStatus status = issueToProcess.getIssue().getStatus();
        if (issueToProcess.isDefined() && ( status == IssueStatus.CREATED || status == IssueStatus.NEW || status == IssueStatus.ASSIGNED || status == IssueStatus.ON_QA)) {
            LabelItem<?> li = new DefinedLabelItem(expectoPatronum, LabelItem.LabelAction.SET, LabelItem.LabelSeverity.BAD);
            labelData.addLabelItem(li);
        } else {
            LabelItem<?> li = new DefinedLabelItem(expectoPatronum, LabelItem.LabelAction.REMOVE, LabelItem.LabelSeverity.OK);
            labelData.addLabelItem(li);
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

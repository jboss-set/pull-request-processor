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
import org.jboss.set.pull.processor.data.Attribute;
import org.jboss.set.pull.processor.data.DefinedLabelItem;
import org.jboss.set.pull.processor.data.DefinedLabelItem.LabelContent;
import org.jboss.set.pull.processor.data.EvaluatorData;
import org.jboss.set.pull.processor.data.IssueData;
import org.jboss.set.pull.processor.data.LabelData;
import org.jboss.set.pull.processor.data.LabelItem;
import org.jboss.set.pull.processor.data.LabelItem.LabelSeverity;
import org.jboss.set.pull.processor.data.EvaluatorReportEntry;
import org.jboss.set.pull.processor.impl.evaluator.util.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Check what ACK flags we have and set up labels accordingly(or clear).
 *
 * @author baranowb
 *
 */
public abstract class AbstractIssueACKFlagsLabelEvaluator extends AbstractLabelEvaluator {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractIssueACKFlagsLabelEvaluator.class);

    protected abstract String evaluatorLabel();

    protected void processAckLabels(EvaluatorContext context, Attribute<IssueData> issueKey, Attribute<LabelData> labelsKey, EvaluatorData data) {
        String pr = LogUtil.prRef(context.getPullRequest().getURI());
        String eval = LogUtil.pad(evaluatorLabel());
        IssueData issueToProcess = data.getAttributeValue(issueKey);
        if (!issueToProcess.isDefined()) {
            LOG.info("{} | {} | issue not defined, skipping", pr, eval);
            return;
        }
        LabelData labelData = super.getLabelData(labelsKey, data);
        boolean hasAllAcks = true;
        hasAllAcks = hasAllAcks & processFlagStatus(labelData, issueToProcess.getPmAckStatus(), LabelContent.Needs_pm_ack);
        hasAllAcks = hasAllAcks & processFlagStatus(labelData, issueToProcess.getDevAckStatus(), LabelContent.Needs_devel_ack);
        hasAllAcks = hasAllAcks & processFlagStatus(labelData, issueToProcess.getQeAckStatus(), LabelContent.Needs_qa_ack);

        String issueRef = issueToProcess.getIssue().getURI().getPath().replaceFirst(".*/browse/", "");
        LOG.info("{} | {} | issue={} | pm_ack={}, dev_ack={}, qa_ack={} | hasAllAcks={}",
                pr, eval, issueRef,
                issueToProcess.getPmAckStatus(), issueToProcess.getDevAckStatus(),
                issueToProcess.getQeAckStatus(), hasAllAcks);

        EvaluatorReportEntry entry = new EvaluatorReportEntry(evaluatorLabel());
        entry.addField("issue", issueRef, "read");
        entry.addField("pm_ack", String.valueOf(issueToProcess.getPmAckStatus()), "read");
        entry.addField("dev_ack", String.valueOf(issueToProcess.getDevAckStatus()), "read");
        entry.addField("qa_ack", String.valueOf(issueToProcess.getQeAckStatus()), "read");
        entry.addField("hasAllAcks", String.valueOf(hasAllAcks), "computed");
        EvaluatorReportEntry.addTo(data, entry);

        if (hasAllAcks) {
            LabelItem<?> li = new DefinedLabelItem(LabelContent.Has_All_Acks, LabelItem.LabelAction.SET, LabelSeverity.OK);
            labelData.addLabelItem(li);
        } else {
            LabelItem<?> li = new DefinedLabelItem(LabelContent.Has_All_Acks, LabelItem.LabelAction.REMOVE, LabelItem.LabelSeverity.BAD);
            labelData.addLabelItem(li);
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


}

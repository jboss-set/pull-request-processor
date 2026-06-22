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
import org.jboss.set.pull.processor.data.Attribute;
import org.jboss.set.pull.processor.data.DefinedLabelItem;
import org.jboss.set.pull.processor.data.DefinedLabelItem.LabelContent;
import org.jboss.set.pull.processor.data.EvaluatorData;
import org.jboss.set.pull.processor.data.IssueData;
import org.jboss.set.pull.processor.data.LabelData;
import org.jboss.set.pull.processor.data.LabelItem;
import org.jboss.set.pull.processor.data.EvaluatorReportEntry;
import org.jboss.set.pull.processor.impl.evaluator.util.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Set labels based on presence of issues.
 *
 * @author baranowb
 *
 */
public abstract class AbstractIssuePresentLabelEvaluator extends AbstractLabelEvaluator {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractIssuePresentLabelEvaluator.class);

    protected abstract String evaluatorLabel();

    protected void processPresenceLabel(EvaluatorContext context, Attribute<IssueData> issueKey, Attribute<LabelData> labelsKey, LabelContent expectoPatronum, EvaluatorData data) {
        String pr = LogUtil.prRef(context.getPullRequest().getURI());
        String eval = LogUtil.pad(evaluatorLabel());
        LabelData labelData = super.getLabelData(labelsKey, data);
        final IssueData issueToProcess = data.getAttributeValue(issueKey);
        String action;
        if (issueToProcess.isDefined() || !issueToProcess.isRequired()) {
            LabelItem<?> li = new DefinedLabelItem(expectoPatronum, LabelItem.LabelAction.REMOVE, LabelItem.LabelSeverity.OK);
            labelData.addLabelItem(li);
            action = "REMOVE";
        } else {
            LabelItem<?> li = new DefinedLabelItem(expectoPatronum, LabelItem.LabelAction.SET, LabelItem.LabelSeverity.BAD);
            labelData.addLabelItem(li);
            action = "SET";
        }
        LOG.info("{} | {} | defined={}, required={} | {} {}",
                pr, eval, issueToProcess.isDefined(), issueToProcess.isRequired(),
                action, expectoPatronum);

        EvaluatorReportEntry entry = new EvaluatorReportEntry(evaluatorLabel());
        entry.addField("defined", String.valueOf(issueToProcess.isDefined()), "read");
        entry.addField("required", String.valueOf(issueToProcess.isRequired()), "read");
        entry.addField("action", action + " " + expectoPatronum, "computed");
        EvaluatorReportEntry.addTo(data, entry);
    }

}

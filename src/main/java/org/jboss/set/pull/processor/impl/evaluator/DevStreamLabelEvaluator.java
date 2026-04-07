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

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jboss.set.aphrodite.domain.Codebase;
import org.jboss.set.pull.processor.EvaluatorContext;
import org.jboss.set.pull.processor.data.Attribute;
import org.jboss.set.pull.processor.data.Attributes;
import org.jboss.set.pull.processor.data.CodeBaseLabelItem;
import org.jboss.set.pull.processor.data.EvaluatorData;
import org.jboss.set.pull.processor.data.LabelData;
import org.jboss.set.pull.processor.data.LabelItem.LabelAction;
import org.jboss.set.pull.processor.data.LabelItem.LabelSeverity;

/**
 * Set dev labels PR. This include branch of PR and label derived from stream name.
 *
 * @author baranowb
 *
 */
public class DevStreamLabelEvaluator extends AbstractLabelEvaluator {
    // digit.digit|lowercase.digit|lowercase
    private static final Pattern STREAM_PATTERN = Pattern.compile("[\\d]\\.[\\d\\w]\\.[\\d\\w]");

    @Override
    public void eval(EvaluatorContext context, EvaluatorData data) {
        LabelData labelData = super.getLabelData(Attributes.LABELS_CURRENT, data);
        CodeBaseLabelItem branchLabel = new CodeBaseLabelItem(context.getPullRequest().getCodebase(), LabelAction.SET,
                LabelSeverity.OK);
        labelData.addLabelItem(branchLabel);
        Matcher matcher = STREAM_PATTERN .matcher(context.getStreamComponentDefinition().getStreamDefinition().getStream().getName());
        if (matcher.find()) {
            // example "jboss-eap-7.z.0" --> "7.z.0.GA"
            CodeBaseLabelItem streamLabel = new CodeBaseLabelItem(new Codebase(matcher.group() + ".GA"), LabelAction.SET, LabelSeverity.OK);
            labelData.addLabelItem(streamLabel);
        }
    }

    @Override
    public List<Attribute<?>> getRequiredAttributes() {
        return List.of(Attributes.LABELS_CURRENT);
    }
}

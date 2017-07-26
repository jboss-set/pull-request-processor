package org.jboss.set.pull.processor.data;

import org.jboss.set.aphrodite.domain.Codebase;

public class CodeBaseLabelItem extends LabelItem<Codebase> {

    public CodeBaseLabelItem(Codebase label, LabelAction action, LabelSeverity severity) {
        super(label, action, severity);
    }

    @Override
    public String getLabel() {
        return super.label.getName();
    }

}

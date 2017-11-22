package org.jboss.set.pull.processor.data;


public class DefinedLabelItem extends LabelItem<DefinedLabelItem.LabelContent> {

    public DefinedLabelItem(LabelContent label, LabelAction action, LabelSeverity severity) {
        super(label, action, severity);
    }

    @Override
    public String getLabel() {
        return super.label.toString();
    }
    /**
     * Static content that can be used as label
     * @author baranowb
     *
     */
    public static enum LabelContent {
        Needs_devel_ack("Needs devel_ack"),
        Needs_pm_ack("Needs pm_ack"),
        Needs_qa_ack("Needs qa_ack"),
        Has_All_Acks("Has All Acks"),
        Upstream_merged("upstream merged"),
        Missing_upstream_issue("Missing upstream issue"),
        Missing_issue("Missing issue"),
        Missing_upstream_PR("Missing upstream PR"),
        Corrupted_upgrade_meta("Corrupted upgrade meta");

        private String label;

        LabelContent(String label) {
            this.label = label;
        }

        public String toString() {
            return label;
        }
    }
}

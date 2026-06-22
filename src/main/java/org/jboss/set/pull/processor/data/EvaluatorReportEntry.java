package org.jboss.set.pull.processor.data;

import java.util.ArrayList;
import java.util.List;

public class EvaluatorReportEntry {

    private final String evaluator;
    private final List<ReportField> fields = new ArrayList<>();

    public EvaluatorReportEntry(String evaluator) {
        this.evaluator = evaluator;
    }

    public void addField(String name, String value, String type) {
        fields.add(new ReportField(name, value, type));
    }

    public String getEvaluator() {
        return evaluator;
    }

    public List<ReportField> getFields() {
        return fields;
    }

    @SuppressWarnings("unchecked")
    public static void addTo(EvaluatorData data, EvaluatorReportEntry entry) {
        List<EvaluatorReportEntry> entries = data.getAttributeValue(Attributes.EVALUATOR_REPORT);
        if (entries == null) {
            entries = new ArrayList<>();
            data.setAttributeValue(Attributes.EVALUATOR_REPORT, entries);
        }
        entries.add(entry);
    }

    public static class ReportField {

        private final String name;
        private final String value;
        private final String type;

        public ReportField(String name, String value, String type) {
            this.name = name;
            this.value = value;
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }

        public String getType() {
            return type;
        }
    }
}

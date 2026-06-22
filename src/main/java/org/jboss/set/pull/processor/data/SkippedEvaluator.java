package org.jboss.set.pull.processor.data;

import java.util.ArrayList;
import java.util.List;

public class SkippedEvaluator {

    private final String evaluator;
    private final List<String> missingAttributes;
    private final List<String> producedAttributes;

    public SkippedEvaluator(String evaluator, List<String> missingAttributes, List<String> producedAttributes) {
        this.evaluator = evaluator;
        this.missingAttributes = missingAttributes;
        this.producedAttributes = producedAttributes;
    }

    public String getEvaluator() {
        return evaluator;
    }

    public List<String> getMissingAttributes() {
        return missingAttributes;
    }

    public List<String> getProducedAttributes() {
        return producedAttributes;
    }

    @SuppressWarnings("unchecked")
    public static void addTo(EvaluatorData data, SkippedEvaluator entry) {
        List<SkippedEvaluator> entries = data.getAttributeValue(Attributes.SKIPPED_EVALUATORS);
        if (entries == null) {
            entries = new ArrayList<>();
            data.setAttributeValue(Attributes.SKIPPED_EVALUATORS, entries);
        }
        entries.add(entry);
    }
}

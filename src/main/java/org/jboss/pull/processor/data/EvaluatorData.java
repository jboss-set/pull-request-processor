package org.jboss.pull.processor.data;

import java.util.HashMap;
import java.util.Map;

public class EvaluatorData {

    private Map<String, Object> data;

    public EvaluatorData(Map<String, Object> data) {
        this.data = data;
    }

    public EvaluatorData() {
        this.data = new HashMap<>();
    }

    public Map<String, Object> getData() {
        return data;
    }

    public <T>  T getAttributeValue(Attribute<T> attr) {
        return ( T)data.get(attr.name());
    }

    public <T> void setAttributeValue(Attribute<T> attr, T value) {
        data.put(attr.name(), value);
    }

}
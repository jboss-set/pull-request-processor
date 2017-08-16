package org.jboss.set.pull.processor.data;


public class Attribute<T> {

    private String name;

    public Attribute(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

}

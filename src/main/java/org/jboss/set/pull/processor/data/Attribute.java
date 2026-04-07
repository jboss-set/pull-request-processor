package org.jboss.set.pull.processor.data;

import java.util.Objects;

public final class Attribute<T> {

    private String name;

    public Attribute(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Attribute<?> other = (Attribute<?>) obj;
        return Objects.equals(name, other.name);
    }

    @Override
    public String toString() {
        return name;
    }

}
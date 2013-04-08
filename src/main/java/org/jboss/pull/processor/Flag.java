package org.jboss.pull.processor;

public class Flag {

    public enum Status {
        /**
         * The {@code UNSET} {@link Status} represents a {@link Flag} which has not been toggled. It is represented by ' '.
         */
        UNSET,

        /**
         * The {@code POSITIVE} {@link Status} represents a {@link Flag} which has been toggled to '+'.
         */
        POSITIVE,

        /**
         * The {@code NEGATIVE} {@link Status} represents a {@link Flag} which has been toggled to '-'.
         */
        NEGATIVE,

        /**
         * The {@code UNKNOWN} {@link Status} represents a {@link Flag} which has been toggled to '?'.
         */
        UNKNOWN
    }

    private String name;
    private String setter;
    private Status status;

    Flag(String name, String setter, Status status) {
        this.name = name;
        this.setter = setter;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSetter() {
        return setter;
    }

    public void setSetter(String setter) {
        this.setter = setter;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String toString() {
        return setter + "\t" + " set " + name + "\t" + " to " + status + "\t\n";
    }

}

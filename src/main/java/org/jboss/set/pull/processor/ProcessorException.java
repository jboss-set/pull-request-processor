package org.jboss.set.pull.processor;


public class ProcessorException extends Exception {

    private static final long serialVersionUID = 1L;

    public ProcessorException(String message, Throwable ex) {
        super(message, ex);
    }

}

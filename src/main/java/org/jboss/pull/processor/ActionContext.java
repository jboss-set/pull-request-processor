package org.jboss.pull.processor;

import java.util.List;

import org.jboss.set.aphrodite.Aphrodite;

public class ActionContext {

    private Aphrodite aphrodite;

    private List<String> streams;

    private List<String> allowedStreams;

    private String fileName;

    private Boolean write;

    public ActionContext(Aphrodite aphrodite, List<String> streams, List<String> allowedStreams, String fileName, Boolean write) {
        this.aphrodite = aphrodite;
        this.streams = streams;
        this.fileName = fileName;
        this.write = write;
        this.allowedStreams = allowedStreams;
    }

    public Aphrodite getAphrodite() {
        return aphrodite;
    }

    public List<String> getStreams() {
        return streams;
    }

    public List<String> getAllowedStreams() {
        return allowedStreams;
    }

    public String getFileName() {
        return fileName;
    }

    public Boolean getWrite() {
        return write;
    }

}

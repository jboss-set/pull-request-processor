package org.jboss.pull.processor.data;

import java.net.URL;
import java.util.List;

public class PullRequestData {
    private URL link;

    private String label;

    private List<String> streams;

    public PullRequestData(String label, List<String> streams, URL link) {
        this.link = link;
        this.label = label;
        this.streams = streams;
    }

    public String getLabel() {
        return label;
    }

    public List<String> getStreams() {
        return streams;
    }

    public URL getLink() {
        return link;
    }
}

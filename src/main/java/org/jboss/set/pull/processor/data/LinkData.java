package org.jboss.set.pull.processor.data;

import java.net.URL;

public class LinkData {
    private URL link;

    private String label;

    public LinkData(String label, URL link) {
        this.link = link;
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public URL getLink() {
        return link;
    }
}
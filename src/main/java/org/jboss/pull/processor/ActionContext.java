package org.jboss.pull.processor;

import org.jboss.set.aphrodite.Aphrodite;
import org.jboss.set.aphrodite.spi.StreamService;

public class ActionContext {

    private Aphrodite aphrodite;
    
    private StreamService streamService;
    

    public ActionContext(Aphrodite aphrodite, StreamService streamService) {
        this.aphrodite = aphrodite;
        this.streamService = streamService;
    }
    
    public Aphrodite getAphrodite() {
        return aphrodite;
    }
    
    public StreamService getStreamService() {
        return streamService;
    }
}

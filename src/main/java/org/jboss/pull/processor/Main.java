package org.jboss.pull.processor;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.logging.Logger;

import org.jboss.pull.processor.data.ProcessorData;
import org.jboss.set.aphrodite.Aphrodite;
import org.jboss.set.aphrodite.JsonStreamService;
import org.jboss.set.aphrodite.domain.Issue;
import org.jboss.set.aphrodite.spi.NotFoundException;
import org.jboss.set.aphrodite.spi.StreamService;

public class Main {

	public static Logger logger = Logger.getLogger("org.jboss.pull.processor");

	
	public void start(String streamName) throws Exception {
	    logger.info("initializing....");
	    try (Aphrodite aphrodite = Aphrodite.instance()){
    		StreamService streamService = getStreamService(aphrodite);

        	List<URL> urls = null;
        	if(streamName == null) {
                logger.info("finding all repositories...");
        		urls = streamService.findAllRepositories();
        	} else {
        	    logger.info("finding all repositories for stream " + streamName);
        		urls = streamService.findAllRepositoriesInStream(streamName);
        	}
        	logger.info("number of repositories found: " + urls.size());
        	ServiceLoader<Processor> processors = ServiceLoader.load(Processor.class);
        	List<ProcessorData> data = new ArrayList<>();
        	
        	for(Processor processor : processors) {
        	    logger.info("executing processor: " + processor.getClass().getName());
        		for(URL url : urls) {
    	    		processor.init(aphrodite, streamService);
    	    		data.addAll(processor.process(url));
    	    	}
        	}
        	logger.info("executing actions...");
        	ServiceLoader<Action> actions = ServiceLoader.load(Action.class);
        	ActionContext actionContext = new ActionContext(aphrodite, streamService);
        	for(Action action : actions) {
        	    logger.info("executing processor: " + action.getClass().getName());
        		action.execute(actionContext, data);
        	}
	    } finally {
	        logger.info("finalizing....");
	    }
	}
	

	
	private StreamService getStreamService(Aphrodite aphrodite) throws NotFoundException {
		JsonStreamService service = new JsonStreamService(aphrodite);
		service.loadStreamData();
		return service;
	}
	
    public static void main(String[] argv) throws Exception {
    	new Main().start(argv[0]);
    }

}

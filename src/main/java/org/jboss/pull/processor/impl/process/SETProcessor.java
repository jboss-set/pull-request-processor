package org.jboss.pull.processor.impl.process;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.jboss.pull.processor.Evaluator;
import org.jboss.pull.processor.EvaluatorContext;
import org.jboss.pull.processor.Processor;
import org.jboss.pull.processor.ProcessorException;
import org.jboss.pull.processor.data.EvaluatorData;
import org.jboss.set.aphrodite.Aphrodite;
import org.jboss.set.aphrodite.domain.Issue;
import org.jboss.set.aphrodite.domain.Patch;
import org.jboss.set.aphrodite.domain.PatchState;
import org.jboss.set.aphrodite.domain.Repository;
import org.jboss.set.aphrodite.spi.NotFoundException;

public class SETProcessor implements Processor {

    private static Logger logger = Logger.getLogger("org.jboss.pull.processor.processes");

    private Aphrodite aphrodite;

    private List<Evaluator> rules;

    private ExecutorService service;

    public void init(Aphrodite aphrodite) {
        this.aphrodite = aphrodite;
        this.rules = getRules();
        this.service = Executors.newFixedThreadPool(10);
    }

    public List<EvaluatorData> process(Repository repository) throws ProcessorException {
        try {
            List<Patch> patches = aphrodite.getPatchesByState(repository, PatchState.OPEN);
            
            List<Future<EvaluatorData>> results = this.service.invokeAll(patches.stream().map(e -> new PatchProcessingTask(repository, e)).collect(Collectors.toList()));
            
            List<EvaluatorData> data = new ArrayList<>();
            for(Future<EvaluatorData> result : results) {
                try {
                    data.add(result.get());
                } catch(Exception ex) {
                    logger.log(Level.SEVERE, "ouch !" + ex);
                }
            }

            return data;

        } catch(NotFoundException | InterruptedException ex) {
            throw new ProcessorException("processor execution failed", ex);
        } finally {
            this.service.shutdown();
        }
    }

    private class PatchProcessingTask implements Callable<EvaluatorData> {

        private Repository repository;
        
        private Patch patch;
        
        public PatchProcessingTask(Repository repository, Patch patch) {
            this.repository = repository;
            this.patch = patch;
        }

        @Override
        public EvaluatorData call() throws Exception {
            try {
                logger.info("processing " + patch.getURL().toString());
                List<Issue> issues = aphrodite.getIssuesAssociatedWith(patch);
                List<Patch> relatedPatches = aphrodite.findPatchesRelatedTo(patch);
                EvaluatorContext context = new EvaluatorContext(aphrodite, repository, patch, issues, relatedPatches);
                EvaluatorData data = new EvaluatorData();
                for(Evaluator rule : rules) {
                    logger.fine("repository " + repository.getURL() + "applying evaluator " + rule.name() + " to " + patch.getId());
                    rule.eval(context, data);
                }
                return data;
            } catch (Throwable th) {
                logger.log(Level.SEVERE, "failed to " + patch.getURL(), th);
                throw new Exception(th);
            }
        }
        
    }

    private List<Evaluator> getRules() {
        ServiceLoader<Evaluator> rules = ServiceLoader.load(Evaluator.class);
        List<Evaluator> tmp = new ArrayList<Evaluator>();
        
        for(Evaluator rule : rules) {
            tmp.add(rule);
        }
        
        return tmp;
    }


}

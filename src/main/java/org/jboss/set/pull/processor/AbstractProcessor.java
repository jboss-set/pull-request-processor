/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.set.pull.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.jboss.set.aphrodite.domain.PullRequest;
import org.jboss.set.pull.processor.data.EvaluatorData;
import org.jboss.set.pull.processor.data.PullRequestReference;

/**
 * Abstract class. Base for processors. Provide basic code to allow simpler processor dev.
 *
 * @author baranowb
 *
 */
@SuppressWarnings("static-access")
public abstract class AbstractProcessor implements Processor {

    protected static final Logger LOGGER = Logger.getLogger(AbstractProcessor.class.getPackage().getName());

    protected ProcessorConfig processorConfig;
    // protected Collection<Codebase> permitedBranches;
    protected final String simpleName;

    public AbstractProcessor() {
        super();
        this.simpleName = getClass().getSimpleName();
    }

    public void init(final ProcessorConfig processorConfig) {
        assert processorConfig != null;
        this.processorConfig = processorConfig;
    }

    /**
     * Returh phase for which implementation will work. This is used to pick proper evaluators and actions
     *
     * @return
     */
    public abstract ProcessorPhase getPhase();

    /**
     * Processor can fetch PRs based on any criteria it sees fit and by any means. {@link AbstractProcessor#fetchPullRequests}
     * will call it and perform some filtering.
     *
     * @return
     */
    protected abstract List<PullRequestReference> fetchPullRequestsRaw();

    /**
     * Provide basic filtering of existing PRs and matching codebase to one present in jboss streams.
     */
    private List<PullRequestReference> fetchPullRequests() {
        //NOTE1: check if we dont leak PRs this way?
        //NOTE2: do we even care if we leak them?
        return fetchPullRequestsRaw().stream().filter(pr -> {
            try {
                if (pr.getComponentDefinition().isFound() && pr.getComponentDefinition().getStreamComponent().getCodebase()
                        .equals(pr.getPullRequest().getCodebase()))
                    return true;
                else
                    return false;
            } catch (Exception e) {
                //TODO: XXX hanle it properly
                log(Level.SEVERE, "Failed at: " + pr, e);
                return false;
            }
        }).collect(Collectors.toList());
    }

    public void process() throws ProcessorException {
        try {
            final List<EvaluatorData> processedPullRequests = new ArrayList<>();
            final List<PullRequestReference> pullRequests = fetchPullRequests();
            log(Level.INFO, " processing: " + pullRequests.size() + " PRs");
            List<Future<EvaluatorData>> results = this.processorConfig.getExecutorService()
                    .invokeAll(pullRequests.stream().map(e -> new PullRequestEvaluatorTask(e.getPullRequest(),e.getComponentDefinition())).collect(Collectors.toList()));

            for (Future<EvaluatorData> result : results) {
                try {
                    processedPullRequests.add(result.get());
                } catch (Exception ex) {
                    log(Level.SEVERE, "ouch !", ex);
                }
            }

            log(Level.INFO, "executing actions:");
            List<Action> actions = this.processorConfig.getActions();
            ActionContext actionContext = new ActionContext(this.processorConfig);
            for (Action action : actions) {
                log(Level.INFO, "...." + action.getClass().getName());
                // This means that every processor will have its own set of actions
                // ie. report write will be per processor
                action.execute(actionContext, processedPullRequests);
            }
        } catch (InterruptedException ex) {
            throw new ProcessorException("processor execution failed", ex);
        }
    }

    protected void log(final Level level, final String msg) {
        this.LOGGER.log(level, this.simpleName + " " + msg);
    }

    protected void log(final Level level, final String msg, final Throwable t) {
        this.LOGGER.log(level, this.simpleName + " " + msg, t);
    }

    private class PullRequestEvaluatorTask implements Callable<EvaluatorData> {

        private final PullRequest pullRequest;
        private final StreamComponentDefinition streamComponentDefinition;

        public PullRequestEvaluatorTask(final PullRequest e, final StreamComponentDefinition streamComponentDefinition) {
            this.pullRequest = e;
            this.streamComponentDefinition = streamComponentDefinition;
        }

        @Override
        public EvaluatorData call() throws Exception {
            try {
                log(Level.FINE, "processing " + this.pullRequest.getURL().toString());

                EvaluatorContext context = new EvaluatorContext(processorConfig.getAphrodite(), this.pullRequest,
                        this.streamComponentDefinition);
                EvaluatorData data = new EvaluatorData();
                for (Evaluator rule : processorConfig.getEvaluators()) {
                    LOGGER.fine("repository " + pullRequest.getRepository().getURL()
                            + "applying evaluator " + rule.name() + " to "
                            + this.pullRequest.getId());
                    rule.eval(context, data);
                }
                return data;
            } catch (Throwable th) {
                log(Level.SEVERE, "failed to " + this.pullRequest.getURL(), th);
                throw new Exception(th);
            }
        }

    }
}

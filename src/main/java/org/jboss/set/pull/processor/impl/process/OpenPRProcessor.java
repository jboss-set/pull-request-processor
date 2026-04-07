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
package org.jboss.set.pull.processor.impl.process;

import java.util.ArrayList;
import java.util.List;

import org.jboss.set.aphrodite.domain.PullRequest;
import org.jboss.set.pull.processor.Action;
import org.jboss.set.pull.processor.ActionContext;
import org.jboss.set.pull.processor.Evaluator;
import org.jboss.set.pull.processor.EvaluatorContext;
import org.jboss.set.pull.processor.Processor;
import org.jboss.set.pull.processor.ProcessorConfig;
import org.jboss.set.pull.processor.ProcessorException;
import org.jboss.set.pull.processor.data.Attribute;
import org.jboss.set.pull.processor.data.EvaluatorData;
import org.jboss.set.pull.processor.data.PullRequestReference;
import org.jboss.set.pull.processor.data.ReportItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Process all PRs in OPEN state
 *
 * @author baranowb
 *
 */
public class OpenPRProcessor implements Processor {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenPRProcessor.class);

    protected ProcessorConfig processorConfig;

    public void init(final ProcessorConfig processorConfig) {
        assert processorConfig != null;
        this.processorConfig = processorConfig;
    }

    @Override
    public List<ReportItem> process(PullRequestReference pullRequestReference) throws ProcessorException {
        LOGGER.info("processing pull request {}", pullRequestReference);
        return executeActions(executeEvaluators(pullRequestReference));
    }

    public EvaluatorData executeEvaluators(PullRequestReference pullRequestReference) throws ProcessorException {
        PullRequest pullRequest = pullRequestReference.getPullRequest();
        try {
            LOGGER.info("processing : {}", pullRequestReference.getPullRequest().getURI());
            EvaluatorContext context = new EvaluatorContext(processorConfig.getAphrodite(), pullRequest, pullRequestReference.getComponentDefinition());
            EvaluatorData data = new EvaluatorData();
            List<Attribute<?>> produced = new ArrayList<>();
            List<Evaluator> evaluators = new ArrayList<>(processorConfig.getEvaluators());

            while(!evaluators.isEmpty()) {
                List<Evaluator> toProcess = toExecute(evaluators, produced);
                if (toProcess.isEmpty()) {
                    break;
                }
                evaluators.removeAll(toProcess);
                for (Evaluator rule : toProcess) {
                    produced.addAll(rule.getProducedAttributes());
                    if (data.getAttributes().containsAll(rule.getRequiredAttributes())) {
                        LOGGER.info("repository {} applying evaluator {} to {}", pullRequest.getRepository().getURI(), rule.name(), pullRequestReference);
                        // we only execute of the data contains all the attributes required
                        rule.eval(context, data);
                    } else {
                        List<Attribute<?>> missingAttributes = new ArrayList<>(rule.getRequiredAttributes());
                        missingAttributes.removeAll(data.getAttributes());
                        LOGGER.info("repository {} skipping evaluator {} to {} because of missing attributes {}", pullRequest.getRepository().getURI(), rule.name(), pullRequestReference);
                    }
                }
            }
            return data;
        } catch (Throwable th) {
            LOGGER.error("failed to {}", pullRequest.getURI(), th);
            throw new ProcessorException(th);
        }
    }

    private List<Evaluator> toExecute(List<Evaluator> evaluators, List<Attribute<?>> produced) {
        List<Evaluator> next = new ArrayList<>();
        for (Evaluator evaluator : evaluators) {
            if (produced.containsAll(evaluator.getRequiredAttributes())) {
                next.add(evaluator);
            }
        }
        return next;
    }

    private List<ReportItem> executeActions(EvaluatorData evaluatorData) {
        List<ReportItem> items = new ArrayList<>();
        List<Action> actions = this.processorConfig.getActions();
        ActionContext actionContext = new ActionContext(this.processorConfig);
        for (Action action : actions) {
            ReportItem reportItem = action.execute(actionContext, evaluatorData);
            if (reportItem != null) {
                items.add(reportItem);
            }
        }
        return items;
    }
}

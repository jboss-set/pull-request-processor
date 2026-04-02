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

import java.util.List;

import org.jboss.set.aphrodite.domain.PullRequest;
import org.jboss.set.pull.processor.data.EvaluatorData;
import org.jboss.set.pull.processor.data.PullRequestReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class. Base for processors. Provide basic code to allow simpler processor dev.
 *
 * @author baranowb
 *
 */
public abstract class AbstractProcessor implements Processor {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractProcessor.class);

    protected ProcessorConfig processorConfig;

    public void init(final ProcessorConfig processorConfig) {
        assert processorConfig != null;
        this.processorConfig = processorConfig;
    }

    @Override
    public void process(PullRequestReference pullRequestReference) throws ProcessorException {
        LOGGER.info("processing pull request {}", pullRequestReference);
        executeActions(executeEvaluators(pullRequestReference));
    }

    public EvaluatorData executeEvaluators(PullRequestReference pullRequestReference) throws ProcessorException {
        PullRequest pullRequest = pullRequestReference.getPullRequest();
        try {
            LOGGER.info("processing : {}", pullRequestReference.getPullRequest().getURI());
            EvaluatorContext context = new EvaluatorContext(processorConfig.getAphrodite(), pullRequest, pullRequestReference.getComponentDefinition());
            EvaluatorData data = new EvaluatorData();
            for (Evaluator rule : processorConfig.getEvaluators()) {
                LOGGER.info("repository {} applying evaluator {} to {}", pullRequest.getRepository().getURI(), rule.name(), pullRequestReference);
                rule.eval(context, data);
            }
            return data;
        } catch (Throwable th) {
            LOGGER.error("failed to {}", pullRequest.getURI(), th);
            throw new ProcessorException(th);
        }
    }

    private void executeActions(EvaluatorData evaluatorData) {
        List<Action> actions = this.processorConfig.getActions();
        ActionContext actionContext = new ActionContext(this.processorConfig);
        for (Action action : actions) {
            action.execute(actionContext, evaluatorData);
        }
    }

}

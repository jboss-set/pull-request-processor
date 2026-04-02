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

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.jboss.set.aphrodite.Aphrodite;

/**
 * Class which holds all the bits required for processors. Otherwise we would have to to mutate
 *
 * @author baranowb
 */
public class ProcessorConfig {

    // list of evaluators. Those classes should process given input
    // so actions can do some stuff based on what evaluators gather.
    private List<Evaluator> evaluators;

    // Actual "do something" thingies, like write-label, update stream etc,
    private List<Action> actions;

    private Aphrodite aphrodite;

    // stream defs that were cross checked between user input and streams.json
    // still it contain both valid and invalid versions.
    private List<StreamDefinition> streamDefinition;

    // list of streams that we can tamper with. In essence we can process more
    // to have big picture and tamper with subset
    private List<StreamDefinition> writePermitedStreamDefinition;
    // report file where we can work.
    private File reportFile;

    private boolean review = false;

    private boolean write = false;

    public ProcessorConfig(ProcessorConfigBuilder builder) {
        super();
        this.evaluators = builder.evaluators;
        this.actions = builder.actions;
        this.aphrodite = builder.aphrodite;
        this.streamDefinition = Collections.unmodifiableList(builder.parsedStreams);
        this.writePermitedStreamDefinition = builder.writePermittedStreams;
        this.review = builder.performReviewAction;
        this.write = builder.performWriteOperations;
        this.reportFile = new File(builder.reportFile);
    }

    public List<Evaluator> getEvaluators() {
        return evaluators;
    }

    public List<Action> getActions() {
        return actions;
    }

    public Aphrodite getAphrodite() {
        return aphrodite;
    }

    public List<StreamDefinition> getStreamDefinition() {
        return streamDefinition;
    }

    public List<StreamDefinition> getWritePermitedStreamDefinition() {
        return writePermitedStreamDefinition;
    }

    public File getReportFile() {
        return reportFile;
    }

    public boolean isReview() {
        return review;
    }

    public boolean isWrite() {
        return write;
    }

    public static ProcessorConfigBuilder newProcessConfigBuilder() {
        return new ProcessorConfigBuilder();
    }

    public static class ProcessorConfigBuilder {
        List<StreamDefinition> parsedStreams;
        List<StreamDefinition> writePermittedStreams;
        String reportFile;
        boolean performReviewAction;
        Boolean performWriteOperations;
        List<Evaluator> evaluators;
        List<Action> actions;
        Aphrodite aphrodite;

        public ProcessorConfigBuilder parsedStreams(List<StreamDefinition> parsedStreams) {
            this.parsedStreams = parsedStreams;
            return this;
        }

        public ProcessorConfigBuilder actions(List<Action> actions) {
            this.actions = actions;
            return this;
        }

        public ProcessorConfigBuilder evaluators(List<Evaluator> evaluators) {
            this.evaluators = evaluators;
            return this;
        }

        public ProcessorConfigBuilder writePermittedStreams(List<StreamDefinition> writePermittedStreams) {
            this.writePermittedStreams = writePermittedStreams;
            return this;
        }

        public ProcessorConfigBuilder reportFile(String reportFile) {
            this.reportFile = reportFile;
            return this;
        }

        public ProcessorConfigBuilder aphrodite(Aphrodite aphrodite) {
            this.aphrodite = aphrodite;
            return this;
        }

        public ProcessorConfigBuilder performReviewAction(boolean performReviewAction) {
            this.performReviewAction = performReviewAction;
            return this;
        }

        public ProcessorConfigBuilder performWriteOperations(boolean performWriteOperations) {
            this.performWriteOperations = performWriteOperations;
            return this;
        }

        public ProcessorConfig build() {
            return new ProcessorConfig(this);
        }
    }
}

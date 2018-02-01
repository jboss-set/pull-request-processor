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
import java.util.concurrent.ExecutorService;

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

    private ExecutorService executorService;

    // stream defs that were cross checked between user input and streams.json
    // still it contain both valid and invalid versions.
    private List<StreamDefinition> streamDefinition;

    // list of streams that we can tamper with. In essence we can process more
    // to have big picture and tamper with subset
    private List<StreamDefinition> writePermitedStreamDefinition;
    // root dir where we can work.
    private File rootDirectory;

    private boolean write = false;

    public ProcessorConfig(final List<Evaluator> evaluators, final List<Action> actions,
            final List<StreamDefinition> streamDefinition, final List<StreamDefinition> writePermitedStreamDefinition,
            final Aphrodite aphrodite, final ExecutorService executorService, final String rootDirectory, final boolean write) {
        super();
        this.evaluators = evaluators;
        this.actions = actions;
        this.aphrodite = aphrodite;
        this.executorService = executorService;
        this.streamDefinition = Collections.unmodifiableList(streamDefinition);
        if (writePermitedStreamDefinition != null) {
            this.writePermitedStreamDefinition = Collections.unmodifiableList(writePermitedStreamDefinition);
        } else {
            this.writePermitedStreamDefinition = this.streamDefinition;
        }
        this.rootDirectory = new File(rootDirectory);
        this.write = write;
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

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public List<StreamDefinition> getStreamDefinition() {
        return streamDefinition;
    }

    public List<StreamDefinition> getWritePermitedStreamDefinition() {
        return writePermitedStreamDefinition;
    }

    public File getRootDirectory() {
        return rootDirectory;
    }

    public boolean isWrite() {
        return write;
    }

}

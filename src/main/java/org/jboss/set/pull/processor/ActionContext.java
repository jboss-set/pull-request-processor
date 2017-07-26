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
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.jboss.set.aphrodite.Aphrodite;
import org.jboss.set.aphrodite.domain.PullRequest;

public class ActionContext {

    private final ProcessorConfig processorConfig;

    public ActionContext(final ProcessorConfig processorConfig) {
        this.processorConfig = processorConfig;
    }

    public Aphrodite getAphrodite() {
        return this.processorConfig.getAphrodite();
    }

    public File getRoot() {
        return this.processorConfig.getRootDirectory();
    }

    public boolean isWritePermitted() {
        return this.processorConfig.isWrite();
    }

    public List<StreamDefinition> getAllowedStreams() {
        return this.processorConfig.getWritePermitedStreamDefinition();
    }

    public List<StreamDefinition> getDefinedStreams() {
        return this.processorConfig.getStreamDefinition();
    }

    public ExecutorService getExecutors() {
        return this.processorConfig.getExecutorService();
    }

    public boolean isWritePermitedOn(final PullRequest pullRequest) {
        // match repo and branch vs permited write repo and branch, to see if we
        // should perform any write ops.
        final String pullRequestBranch = pullRequest.getCodebase().getName();
        final URL repoURL = pullRequest.getRepository().getURL(); // repo will have bit without pull/\\d+
        for (StreamDefinition streamDefinition : this.processorConfig.getWritePermitedStreamDefinition()) {
            if (!streamDefinition.isFound()) {
                continue;
            }
            for (StreamComponentDefinition streamComponentDefinition : streamDefinition.getStreamComponents()) {
                if (!streamComponentDefinition.isFound()) {
                    continue;
                }
                try {
                    if (repoURL.toURI().equals(streamComponentDefinition.getStreamComponent().getRepositoryURL())
                            && pullRequestBranch
                                    .equals(streamComponentDefinition.getStreamComponent().getCodebase().getName())) {
                        return true;
                    }
                } catch (URISyntaxException e) {
                    // TODO: XXX this should not happen, add proper log/info output for CI console
                    e.printStackTrace();
                }
            }
        }
        return false;
    }
}

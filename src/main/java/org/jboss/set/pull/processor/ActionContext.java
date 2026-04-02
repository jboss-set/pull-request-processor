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
import java.net.URI;
import java.util.List;

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

    public File getReportFile() {
        return this.processorConfig.getReportFile();
    }

    public boolean isReviewPermitted() {
        return this.processorConfig.isReview();
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

    public boolean isWritePermitedOn(PullRequest pullRequest) {
        // match repo and branch vs permited write repo and branch, to see if we
        // should perform any write ops.
        final String pullRequestBranch = pullRequest.getCodebase().getBranch();
        final URI repoURL = pullRequest.getRepository().getURI(); // repo will have bit without pull/\\d+
        for (StreamDefinition streamDefinition : this.processorConfig.getWritePermitedStreamDefinition()) {
            for (StreamComponentDefinition streamComponentDefinition : streamDefinition.getStreamComponents()) {
                if (repoURL.equals(streamComponentDefinition.getStreamComponent().getRepositoryURI())
                        && pullRequestBranch
                                .equals(streamComponentDefinition.getStreamComponent().getCodebase().getBranch())) {
                    return true;
                }
            }
        }
        return false;
    }
}

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

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.jboss.set.aphrodite.domain.PullRequest;
import org.jboss.set.aphrodite.domain.PullRequestState;
import org.jboss.set.aphrodite.domain.Repository;
import org.jboss.set.aphrodite.spi.NotFoundException;
import org.jboss.set.pull.processor.AbstractProcessor;
import org.jboss.set.pull.processor.ProcessorPhase;
import org.jboss.set.pull.processor.StreamComponentDefinition;
import org.jboss.set.pull.processor.StreamDefinition;
import org.jboss.set.pull.processor.data.PullRequestReference;

/**
 * Process all PRs in OPEN state
 *
 * @author baranowb
 *
 */
@SuppressWarnings("static-access")
public class OpenPRProcessor extends AbstractProcessor {

    public OpenPRProcessor() {
        super();
        // TODO Auto-generated constructor stub
    }

    @Override
    public ProcessorPhase getPhase() {
        return ProcessorPhase.OPEN;
    }

    @Override
    protected List<PullRequestReference> fetchPullRequestsRaw() {
        // TODO: this might be bad idea but we will process all repos here and try to make sense out of it
        final List<PullRequestReference> pullRequests = new ArrayList<>();
        for (StreamDefinition streamDefinition : super.processorConfig.getStreamDefinition()) {
            if (streamDefinition.isFound()) {
                for (StreamComponentDefinition streamComponentDefinition : streamDefinition.getStreamComponents()) {
                    if (streamDefinition.isFound()) {
                        try {
                            final Repository repository = super.processorConfig.getAphrodite()
                                    .getRepository(streamComponentDefinition.getStreamComponent().getRepositoryURL().toURL());
                            if (repository != null) {
                                final List<PullRequest> componentPullRequests = super.processorConfig.getAphrodite()
                                        .getPullRequestsByState(repository, PullRequestState.OPEN);
                                // translate it into refs, add to ret val
                                pullRequests.addAll(componentPullRequests.stream().map(p -> {
                                    return new PullRequestReference(p, streamComponentDefinition);
                                }).collect(Collectors.toList()));
                            } else {
                                super.LOGGER.warning("Did not find repository: "
                                        + streamComponentDefinition.getStreamComponent().getRepositoryURL());
                            }
                        } catch (MalformedURLException e) {
                            super.log(Level.WARNING, "Did not find repo", e);
                        } catch (NotFoundException e) {
                            super.log(Level.WARNING, "Did not find repo", e);
                        }
                    } else {
                        super.log(Level.WARNING, "Component not found, ignoring: " + streamComponentDefinition);
                    }
                }
            }
        }

        return pullRequests;
    }

}

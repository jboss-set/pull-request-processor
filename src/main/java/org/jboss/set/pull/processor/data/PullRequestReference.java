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
package org.jboss.set.pull.processor.data;

import org.jboss.set.aphrodite.domain.PullRequest;
import org.jboss.set.pull.processor.StreamComponentDefinition;

//simple class to hold some usefull stuff around
public class PullRequestReference {

    public PullRequestReference(PullRequest pullRequest, StreamComponentDefinition componentDefinition) {
        super();
        this.pullRequest = pullRequest;
        this.componentDefinition = componentDefinition;
    }

    // PR
    private PullRequest pullRequest;
    // stream that this one belongs to
    private StreamComponentDefinition componentDefinition;

    public PullRequest getPullRequest() {
        return pullRequest;
    }

    public void setPullRequest(PullRequest pullRequest) {
        this.pullRequest = pullRequest;
    }

    public StreamComponentDefinition getComponentDefinition() {
        return componentDefinition;
    }

    public void setComponentDefinition(StreamComponentDefinition componentDefinition) {
        this.componentDefinition = componentDefinition;
    }

    @Override
    public String toString() {
        return "PullRequestReference [pullRequest=" + pullRequest + ", componentDefinition=" + componentDefinition + "]";
    }

}

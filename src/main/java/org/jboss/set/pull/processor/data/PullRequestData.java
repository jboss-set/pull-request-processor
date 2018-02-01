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

public class PullRequestData {
    private final PullRequest pullRequest;
    private final StreamComponentDefinition streamComponentDefinition;
    private boolean required = true;

    public PullRequestData(final PullRequest pullRequest, final StreamComponentDefinition streamComponentDefinition) {
        this.pullRequest = pullRequest;
        this.streamComponentDefinition = streamComponentDefinition;
    }

    public void notRequiered() {
        this.required = false;
    }

    public boolean isRequired() {
        return this.required;
    }

    public boolean isDefined() {
        return this.pullRequest != null;
    }

    public PullRequest getPullRequest() {
        return pullRequest;
    }

    public StreamComponentDefinition getStreamComponentDefinition() {
        return streamComponentDefinition;
    }

    public boolean isMerged() {
        if (isDefined() && this.pullRequest.isMerged()) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isMergeable() {
        if (isDefined() && this.pullRequest.isMergeable()) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isUpgrade() {
        if (isDefined() && this.pullRequest.isUpgrade()) {
            return true;
        } else {
            return false;
        }
    }
}

/*
 * JBoss, Home of Professional Open Source.
 * Copyright (c) 2013, Red Hat, Inc., and individual contributors
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
package org.jboss.pull.processor.processes;

import org.jboss.pull.processor.Common;
import org.jboss.pull.shared.PullHelper;
import org.jboss.pull.shared.connectors.RedhatPullRequest;
import org.jboss.pull.shared.spi.PullEvaluator.Result;

/**
 * Pull request processor derived from Jason's pull-player. It checks all the open PRs whether they are merge-able and schedule
 * a merge job on Hudson for them. A merge-able PR must be approved by a comment "review ok" and must comply to
 * org.jboss.pull.shared.PullHelper#isMergeable(). It also checks the status of the latest merge job run on Hudson and post
 * comments on github accordingly, etc.
 *
 * @author <a href="mailto:istudens@redhat.com">Ivo Studensky</a>
 * @author Jason T. Greene
 */
public abstract class Processor {

    protected PullHelper helper;

    public void setHelper(PullHelper helper) {
        this.helper = helper;
    }

    public Processor() throws Exception {
        helper = new PullHelper("processor.properties.file", "./processor-eap-7.properties.example");

        // system property "dryrun=true"
        if (Common.isDryRun()) {
            System.out.println("Running in a dry run mode.");
        }
    }

    public abstract Result processPullRequest(RedhatPullRequest pullRequest);

}

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

import java.util.ArrayList;
import java.util.List;

import org.jboss.set.aphrodite.domain.Flag;
import org.jboss.set.aphrodite.domain.FlagStatus;
import org.jboss.set.aphrodite.domain.Issue;

public class IssueData {
    private Issue issue;

    // list of streams that have been + in issue,
    // ie 7.0.z.GA, 7.1.0.GA or 7.backlog.GA
    // JIRA can have one, though BZ can have more ?
    private List<String> streamsAckedInIssue;

    // required flag, default is true, but for upstream in might not be.
    private boolean required = true;

    private FlagStatus devAckStatus, qeAckStatus, pmAckStatus;

    public IssueData() {
        this.issue = null;
        this.streamsAckedInIssue = new ArrayList<>();
        this.devAckStatus = FlagStatus.NO_SET;
        this.qeAckStatus = FlagStatus.NO_SET;
        this.pmAckStatus = FlagStatus.NO_SET;
    }

    public IssueData(final Issue issue, List<String> streams) {
        this.issue = issue;
        this.streamsAckedInIssue = streams;
        FlagStatus status = issue.getStage().getStatus(Flag.DEV);
        setDevAckStatus(status);
        status = issue.getStage().getStatus(Flag.QE);
        setQeAckStatus(status);
        status = issue.getStage().getStatus(Flag.PM);
        setPmAckStatus(status);
    }

    public List<String> getStreams() {
        return streamsAckedInIssue;
    }

    public void notRequired() {
        this.required = false;
    }

    public boolean isRequired() {
        return this.required;
    }

    public boolean isDefined() {
        return this.issue != null;
    }

    public FlagStatus getDevAckStatus() {
        return devAckStatus;
    }

    public void setDevAckStatus(FlagStatus devAckStatus) {
        this.devAckStatus = devAckStatus;
    }

    public FlagStatus getQeAckStatus() {
        return qeAckStatus;
    }

    public void setQeAckStatus(FlagStatus qeAckStatus) {
        this.qeAckStatus = qeAckStatus;
    }

    public FlagStatus getPmAckStatus() {
        return pmAckStatus;
    }

    public void setPmAckStatus(FlagStatus pmAckStatus) {
        this.pmAckStatus = pmAckStatus;
    }

    public List<String> getStreamsAckedInIssue() {
        return streamsAckedInIssue;
    }

    public Issue getIssue() {
        return issue;
    }

    @Override
    public String toString() {
        return "IssueData [issue=" + (issue!=null?issue.getURL(): "NA") + ", streamsAckedInIssue=" + streamsAckedInIssue + ", required=" + required
                + ", devAckStatus=" + devAckStatus + ", qeAckStatus=" + qeAckStatus + ", pmAckStatus=" + pmAckStatus + "]";
    }

}
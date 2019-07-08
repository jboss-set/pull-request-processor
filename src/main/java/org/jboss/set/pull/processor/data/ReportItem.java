/*
 * Copyright 2019 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.set.pull.processor.data;

import java.util.List;

/**
 * @author wangc
 *
 */
public class ReportItem {

    private String url;
    private String issue;
    private List<String> currentLabels;
    private List<String> addLabels;
    private List<String> removeLabels;

    public ReportItem(final String url, final String issue, List<String> currentLabels, List<String> addLabels, List<String> removeLabels) {
        this.url = url;
        this.issue = issue;
        this.currentLabels = currentLabels;
        this.addLabels = addLabels;
        this.removeLabels = removeLabels;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return the issue
     */
    public String getIssue() {
        return issue;
    }

    /**
     * @param issue the issue to set
     */
    public void setIssue(String issue) {
        this.issue = issue;
    }

    /**
     * @return the currentLabels
     */
    public List<String> getCurrentLabels() {
        return currentLabels;
    }

    /**
     * @param currentLabels the currentLabels to set
     */
    public void setCurrentLabels(List<String> currentLabels) {
        this.currentLabels = currentLabels;
    }

    /**
     * @return the addLabels
     */
    public List<String> getAddLabels() {
        return addLabels;
    }

    /**
     * @param addLabels the addLabels to set
     */
    public void setAddLabels(List<String> addLabels) {
        this.addLabels = addLabels;
    }

    /**
     * @return the removeLabels
     */
    public List<String> getRemoveLabels() {
        return removeLabels;
    }

    /**
     * @param removeLabels the removeLabels to set
     */
    public void setRemoveLabels(List<String> removeLabels) {
        this.removeLabels = removeLabels;
    }

}

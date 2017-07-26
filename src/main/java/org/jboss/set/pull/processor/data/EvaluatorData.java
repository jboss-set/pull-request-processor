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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EvaluatorData {

    private Map<String, Object> data;

    public EvaluatorData(Map<String, Object> data) {
        this.data = data;
    }

    public EvaluatorData() {
        this.data = new HashMap<>();
    }

    public Map<String, Object> getData() {
        return data;
    }

    public <T> T getAttributeValue(Attribute<T> attr) {
        return (T) data.get(attr.name());
    }

    public <T> void setAttributeValue(Attribute<T> attr, T value) {
        data.put(attr.name(), value);
    }

    public static final class Attribute<T> {

        private String name;

        public Attribute(String name) {
            this.name = name;
        }

        public String name() {
            return name;
        }

    }

    public static final class Attributes {
        public static final Attribute<Boolean> WRITE_PERMISSION = new Attribute<>("write");
        public static final Attribute<IssueData> ISSUE_CURRENT = new Attribute<>("issue_current");
        public static final Attribute<IssueData> ISSUE_UPSTREAM = new Attribute<>("issue_upstream");
        public static final Attribute<List<IssueData>> ISSUES_RELATED = new Attribute<>("issues_related");
        public static final Attribute<PullRequestData> PULL_REQUEST_CURRENT = new Attribute<>("pr_current");
        public static final Attribute<PullRequestData> PULL_REQUEST_UPSTREAM = new Attribute<>("pr_upstream");
        public static final Attribute<LabelData> LABELS_CURRENT = new Attribute<>("labels_current");
        public static final Attribute<LabelData> LABELS_UPSTREAM = new Attribute<>("labels_upstream");
    }
}
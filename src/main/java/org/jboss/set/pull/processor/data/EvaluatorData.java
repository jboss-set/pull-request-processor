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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EvaluatorData {

    private Map<Attribute<?>, Object> data;

    public EvaluatorData() {
        this(new HashMap<>());
    }

    public EvaluatorData(Map<Attribute<?>, Object> data) {
        this.data = data;
    }

    public boolean hasAttribute(Attribute<?> attr) {
        return data.containsKey(attr);
    }

    public <T> T getAttributeValue(Attribute<T> attr) {
        return (T) data.get(attr);
    }

    public <T> void setAttributeValue(Attribute<T> attr, T value) {
        data.put(attr, value);
    }

    public List<Attribute<?>> getAttributes() {
        return new ArrayList<>(data.keySet());
    }

}
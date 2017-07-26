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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.jboss.set.aphrodite.domain.Stream;

/**
 * Class which represents stream definition with components to process. Expected input:<br>
 * 'streamName[comp1,comp2]'.<br>
 * Example: <br>
 * 'jboss-eap-7.0.z[jbossas-jboss-eap7,jbossas-wildfly-core-eap]'<br>
 * stream name and component must match entries in streams file/resource.
 *
 * @author baranowb
 *
 */
public class StreamDefinition {
    private final String name;
    private Stream stream;

    private final List<StreamComponentDefinition> streamComponents;

    public StreamDefinition(final String def) {
        int index = def.indexOf('[');
        if (index == -1) {
            this.name = def;
            this.streamComponents = new ArrayList<>();
        } else {
            this.name = def.substring(0, index);
            // split components into list and lambda the hell out of it into wrapper
            this.streamComponents = Arrays.asList(def.substring(index + 1).replace("]", "").split(",")).stream()
                    .map(s -> new StreamComponentDefinition(s, this)).collect(Collectors.toList());
        }
    }

    public String getName() {
        return name;
    }

    public List<StreamComponentDefinition> getStreamComponents() {
        return streamComponents;
    }

    public boolean isFound() {
        return this.stream != null;
    }

    public Stream getStream() {
        return stream;
    }

    public void setStream(Stream stream) {
        this.stream = stream;
    }

    @Override
    public String toString() {
        return "StreamDefinition [name=" + name + ", found=" + isFound() + ", streamComponents=" + streamComponents + "]";
    }

}
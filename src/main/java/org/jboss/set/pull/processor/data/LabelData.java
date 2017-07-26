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
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class LabelData {
    // TODO: split per action?
    private Set<LabelItem<?>> labels = new TreeSet<>(new Comparator<LabelItem<?>>() {

        @Override
        public int compare(LabelItem o1, LabelItem o2) {
            if (o1 == null) {
                return -1;
            }
            if (o2 == null) {
                return 1;
            }
            if (o1.equals(o2)) {
                return 0;
            }
            return o1.getLabel().toString().compareTo(o2.getLabel().toString());

        }
    });

    public void addLabelItem(final LabelItem<?> li) {
        if (this.labels.contains(li)) {
            throw new IllegalArgumentException(li.toString());
        }
        this.labels.add(li);
    }

    public List<LabelItem<?>> getLabels(LabelItem.LabelAction act) {
        return labels.stream().filter(l -> l.getAction().equals(act)).collect(Collectors.toList());
    }

    public List<LabelItem<?>> getLabels() {
        return new ArrayList<LabelItem<?>>(labels);
    }
}

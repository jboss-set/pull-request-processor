/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2018, Red Hat, Inc., and individual contributors
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
package org.jboss.set.pull.processor.impl.evaluator.util;

import java.util.List;
import java.util.logging.Logger;

import org.jboss.set.aphrodite.Aphrodite;
import org.jboss.set.aphrodite.domain.Stream;
import org.jboss.set.aphrodite.domain.StreamComponent;
import org.jboss.set.aphrodite.spi.NotFoundException;
import org.jboss.set.pull.processor.StreamComponentDefinition;
import org.jboss.set.pull.processor.StreamDefinition;

public class StreamDefinitionUtil {

    private static Logger logger = Logger.getLogger(StreamDefinitionUtil.class.getPackage().getName());
    /**
     * Fetch stream/component definition from aphrodite if it match conf parameters(StreamDefinition and StreamComponentDefinition)
     * @param aphrodite
     * @param defs
     * @throws NotFoundException
     */
    public static void matchStreams(final Aphrodite aphrodite, final List<StreamDefinition> defs) throws NotFoundException {
        for (StreamDefinition streamDefinition : defs) {
            logger.info("finding all repositories for stream " + streamDefinition);
            Stream stream = aphrodite.getStream(streamDefinition.getName());
            if (stream == null) {
                logger.warning("No stream present for " + streamDefinition);
                continue;
            } else {
                streamDefinition.setStream(stream);
                for (StreamComponentDefinition streamComponentDefinition : streamDefinition.getStreamComponents()) {
                    final StreamComponent streamComponent = stream.getComponent(streamComponentDefinition.getName());
                    if (streamComponent == null) {
                        logger.warning("No component for stream '" + streamDefinition.getName() + "' under '"
                                + streamComponentDefinition + "'");
                        continue;
                    } else {
                        streamComponentDefinition.setStreamComponent(streamComponent);
                    }
                }
            }
        }
    }
}

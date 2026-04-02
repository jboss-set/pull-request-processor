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
import java.util.List;import org.jboss.set.aphrodite.Aphrodite;

import static java.util.stream.Collectors.toList;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

public class Main {

    public static void main(String[] args) throws Exception {
        ArgumentParser parser = ArgumentParsers.newArgumentParser("pull processor");
        try (Aphrodite aphrodite = Aphrodite.instance();){
            parser.addArgument("-s", "--streams").nargs("*").required(true).help(
                    "Specify streams to be processed. Format of entry: stream[component,component],stream[component,component]");
            parser.addArgument("-p", "--permitted").nargs("*").required(false).help(
                    "Specify streams/components that are eligible for write. Format of entry: stream[component,component],stream[component,component]");
            parser.addArgument("-f", "--file").required(true).help("File where save the feed report");
            parser.addArgument("-r", "--review").setDefault(Boolean.FALSE).type(Boolean.class)
                    .help("Determine if pull request review action is performed.");
            parser.addArgument("-w", "--write").setDefault(Boolean.FALSE).type(Boolean.class)
                    .help("Determine if processors should perform write operation on resources or run locally only. ");

            Namespace ns = parser.parseArgs(args);
            // stream[component,component],stream[component,component]
            List<StreamDefinition> streams = ns.<String> getList("streams").stream().map(StreamDefinition::new).collect(toList());
            List<String> permitted = ns.<String> getList("permitted");
            List<StreamDefinition> writePermittedStreams = new ArrayList<>();
            if (permitted != null) {
                writePermittedStreams.addAll(permitted.stream().map(StreamDefinition::new).collect(toList()));
            } else {
                writePermittedStreams.addAll(streams);
            }
            String reportFile = ns.getString("file");
            boolean performReviewAction = ns.getBoolean("review");
            Boolean performWriteOperations = ns.getBoolean("write");

            PullProcessor pullProcessor = PullProcessor.newPullProcessorBuilder()
                    .withAphrodite(aphrodite)
                    .withStreams(streams)
                    .withPermitted(writePermittedStreams)
                    .withReportFile(reportFile)
                    .withPerformReviewAction(performReviewAction)
                    .withPerformWriteAction(performWriteOperations)
                    .build();
            pullProcessor.start();
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }
    }
}

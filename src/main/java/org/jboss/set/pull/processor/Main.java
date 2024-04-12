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
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.jboss.set.aphrodite.Aphrodite;
import org.jboss.set.aphrodite.domain.spi.PullRequestHome;
import org.jboss.set.aphrodite.repository.services.github.GithubPullRequestHomeService;
import org.jboss.set.aphrodite.simplecontainer.SimpleContainer;
import org.jboss.set.pull.processor.impl.evaluator.util.StreamDefinitionUtil;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

public class Main {
    private static final  Logger logger = Logger.getLogger(Main.class.getPackage().getName());
    private static final SimpleContainer simpleContainer = (SimpleContainer) SimpleContainer.instance();

    public void start(List<StreamDefinition> parsedStreams, List<StreamDefinition> writePermittedStreams, String reportFile,
            boolean performReviewAction, Boolean performWriteOperations) throws Exception {
        logger.info("initializing....");
        try (Aphrodite aphrodite = Aphrodite.instance();
             // The Jira rate limit currently imposed is 1 call per 2 seconds per node per user. The current design violate above limitation.
             // Unfortunately I have to make this single thread to make sure evaluators don't break rate limit.
                ClosableHackForExecutor executor = new ClosableHackForExecutor(Executors.newFixedThreadPool(1));) {

            simpleContainer.register(Aphrodite.class.getSimpleName(), aphrodite);
            GithubPullRequestHomeService GithubPullRequestHomeService = new GithubPullRequestHomeService(aphrodite);
            simpleContainer.register(PullRequestHome.class.getSimpleName(), GithubPullRequestHomeService);

            if (parsedStreams.isEmpty()) {
                // this shouldnt happen
                logger.info("No streams specified, can't work like that, make up your mind");
                return;
            } else {
                // XXX: this is a bit scechy, but should do for first iteration
                StreamDefinitionUtil.matchStreams(aphrodite, parsedStreams);
            }

            if (writePermittedStreams != null && !writePermittedStreams.isEmpty()) {
                StreamDefinitionUtil.matchStreams(aphrodite, writePermittedStreams);
            }

            logger.info("loading evaluators:");
            ServiceLoader<Evaluator> evaluatorsServiceLoader = ServiceLoader.load(Evaluator.class);
            final List<Evaluator> evaluatorServices = new ArrayList<Evaluator>();

            for (Evaluator evaluator : evaluatorsServiceLoader) {
                evaluatorServices.add(evaluator);
                logger.info("...." + evaluator.getClass().getSimpleName());
            }

            logger.info("loading actions:");
            ServiceLoader<Action> actionServiceLoader = ServiceLoader.load(Action.class);
            final List<Action> actionServices = new ArrayList<Action>();
            for (Action action : actionServiceLoader) {
                actionServices.add(action);
                logger.info("...." + action.getClass().getSimpleName());
            }

            ServiceLoader<Processor> processors = ServiceLoader.load(Processor.class);
            logger.info("configuring processors:");
            // yeah, two loops, could be done in one go, but KISS is life.
            for (Processor processor : processors) {
                logger.info("...." + processor.getClass().getName());
                final ProcessorPhase processorPhase = processor.getPhase();
                // find actions and evaluators that can be run by processor
                final List<Action> filteredActions = actionServices.stream().filter(a -> a.support(processorPhase))
                        .collect(Collectors.toList());
                final List<Evaluator> filteredEvaluators = evaluatorServices.stream().filter(a -> a.support(processorPhase))
                        .collect(Collectors.toList());
                final ProcessorConfig processorConfig = new ProcessorConfig(filteredEvaluators, filteredActions, parsedStreams,
                        writePermittedStreams, aphrodite, executor.executorService, reportFile, performReviewAction, performWriteOperations);
                processor.init(processorConfig);
            }

            logger.info("executing processors: ");
            for (Processor processor : processors) {
                logger.info("...." + processor.getClass().getName());
                processor.process();
            }
        } finally {
            logger.info("finalizing.");
        }
    }

    private static class ClosableHackForExecutor implements AutoCloseable {
        private ExecutorService executorService;

        public ClosableHackForExecutor(ExecutorService executorService) {
            this.executorService = executorService;
        }

        @Override
        public void close() throws Exception {
            if (this.executorService != null && !this.executorService.isShutdown()) {
                this.executorService.shutdown();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        ArgumentParser parser = ArgumentParsers.newArgumentParser("pull processor");
        parser.addArgument("-s", "--streams").nargs("*").required(true).help(
                "Specify streams to be processed. Format of entry: stream[component,component],stream[component,component]");
        parser.addArgument("-p", "--permitted").nargs("*").required(false).help(
                "Specify streams/components that are eligible for write. Format of entry: stream[component,component],stream[component,component]");
        parser.addArgument("-f", "--file").required(true).help("File where save the feed report");
        parser.addArgument("-r", "--review").setDefault(Boolean.FALSE).type(Boolean.class)
                .help("Determine if pull request review action is performed.");
        parser.addArgument("-w", "--write").setDefault(Boolean.FALSE).type(Boolean.class)
                .help("Determine if processors should perform write operation on resources or run locally only. ");
        // parser.addArgument("-as", "--allowed-streams").nargs("*").required(true).help("jira allowed to be tagged in the
        // repos");
        Namespace ns = null;
        try {
            ns = parser.parseArgs(args);
            // stream[component,component],stream[component,component]
            List<String> streams = ns.getList("streams");
            List<StreamDefinition> parsedStreams = streams.stream().map(e -> new StreamDefinition(e))
                    .collect(Collectors.toList());
            streams = ns.getList("permitted");
            List<StreamDefinition> writePermittedStreams = null;
            if (streams != null)
                writePermittedStreams = streams.stream().map(e -> new StreamDefinition(e)).collect(Collectors.toList());
            String reportFile = ns.getString("file");
            boolean performReviewAction = ns.getBoolean("review");
            Boolean performWriteOperations = ns.getBoolean("write");
            // List<String> allowedStreams = ns.getList("allowed_streams");
            new Main().start(parsedStreams, writePermittedStreams, reportFile, performReviewAction, performWriteOperations);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }
    }
}

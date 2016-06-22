package org.jboss.pull.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.logging.Logger;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import org.jboss.pull.processor.data.EvaluatorData;
import org.jboss.set.aphrodite.Aphrodite;
import org.jboss.set.aphrodite.domain.Repository;

public class Main {

    public static Logger logger = Logger.getLogger("org.jboss.pull.processor");


    public void start(List<String> streams, List<String> allowedStreams, String fileName, Boolean dryrun) throws Exception {
        logger.info("initializing....");
        try (Aphrodite aphrodite = Aphrodite.instance()){

            final List<Repository> repositories = new ArrayList<>();
            if(streams.isEmpty()) {
                logger.info("finding all repositories...");
                repositories.addAll(aphrodite.getDistinctURLRepositoriesFromStreams());
            } else {
                for(String stream : streams) {
                    logger.info("finding all repositories for stream " + stream);
                    aphrodite.getDistinctURLRepositoriesByStream(stream).stream()
                        .filter(e -> !repositories.contains(e))
                        .forEach(e -> repositories.add(e));
                }
            }
            logger.info("number of repositories found: " + repositories.size());
            ServiceLoader<Processor> processors = ServiceLoader.load(Processor.class);
            List<EvaluatorData> data = new ArrayList<>();

            for(Processor processor : processors) {
                logger.info("executing processor: " + processor.getClass().getName());
                for(Repository repository : repositories) {
                    processor.init(aphrodite, allowedStreams);
                    data.addAll(processor.process(repository));
                }
            }

            logger.info("executing actions...");
            ServiceLoader<Action> actions = ServiceLoader.load(Action.class);
            ActionContext actionContext = new ActionContext(aphrodite, streams, allowedStreams, fileName, dryrun);
            for(Action action : actions) {
                logger.info("executing processor: " + action.getClass().getName());
                action.execute(actionContext, data);
            }
        } finally {
            logger.info("finalizing....");
        }
    }

    public static void main(String[] args) throws Exception {
        ArgumentParser parser = ArgumentParsers.newArgumentParser("pull processor");
        parser.addArgument("-s", "--streams")
                .nargs("*")
                .required(true)
                .help("Specify streams to be processed");
        parser.addArgument("-r", "--report")
                .required(true)
                .help("File where save the feed report");
        parser.addArgument("-w", "--write")
                .setDefault(Boolean.FALSE)
                .type(Boolean.class)
                .help("execute in dryRun mode");
        parser.addArgument("-as", "--allowed-streams")
                .nargs("*")
                .required(true)
                .help("jira allowed to be tagged in the repos");
        Namespace ns = null;
        try {
            ns = parser.parseArgs(args);
            List<String> streams = ns.getList("streams");
            String reportFileName = ns.getString("report");
            Boolean dryRun = ns.getBoolean("write");
            List<String> allowedStreams = ns.getList("allowed_streams");
            new Main().start(streams, allowedStreams, reportFileName, dryRun);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }

    }
}

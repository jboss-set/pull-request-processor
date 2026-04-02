package org.jboss.set.pull.processor;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import org.jboss.set.aphrodite.Aphrodite;
import org.jboss.set.aphrodite.container.Container;
import org.jboss.set.aphrodite.domain.Codebase;
import org.jboss.set.aphrodite.domain.PullRequest;
import org.jboss.set.aphrodite.domain.PullRequestState;
import org.jboss.set.aphrodite.domain.Repository;
import org.jboss.set.aphrodite.domain.spi.PullRequestHome;
import org.jboss.set.aphrodite.repository.services.github.GithubPullRequestHomeService;
import org.jboss.set.aphrodite.spi.NotFoundException;
import org.jboss.set.pull.processor.data.PullRequestReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.jboss.set.pull.processor.impl.evaluator.util.StreamDefinitionUtil.matchStreams;

public class PullProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(PullProcessor.class);

    private List<StreamDefinition> parsedStreams;
    private List<StreamDefinition> writePermittedStreams;
    private boolean performWriteAction;
    private boolean performReviewAction;
    private String reportFile;
    private Aphrodite aphrodite;
    private List<Processor> processors;

    private PullProcessor(PullProcessorBuilder builder) {
        this.parsedStreams = builder.parsedStreams;
        this.writePermittedStreams = builder.writePermittedStreams;
        this.performReviewAction = builder.performReviewAction;
        this.performWriteAction = builder.performWriteAction;
        this.reportFile = builder.reportFile;
        this.aphrodite = builder.aphrodite;
        this.processors = init();
    }

    private List<Processor> init() {
        try {
            Container container = Container.instance();
            LOG.info("initializing....");

            container.register(Aphrodite.class.getSimpleName(), aphrodite);
            GithubPullRequestHomeService GithubPullRequestHomeService = new GithubPullRequestHomeService(aphrodite);
            container.register(PullRequestHome.class.getSimpleName(), GithubPullRequestHomeService);

            ServiceLoader<Evaluator> evaluatorsServiceLoader = ServiceLoader.load(Evaluator.class);
            List<Evaluator> evaluatorServices = new ArrayList<Evaluator>();

            LOG.info("Loading evaluator");
            for (Evaluator evaluator : evaluatorsServiceLoader) {
                evaluatorServices.add(evaluator);
                LOG.info("Loading evaluator: {}", evaluator.getClass().getSimpleName());
            }

            LOG.info("Loading actions");
            ServiceLoader<Action> actionServiceLoader = ServiceLoader.load(Action.class);
            List<Action> actionServices = new ArrayList<Action>();
            for (Action action : actionServiceLoader) {
                actionServices.add(action);
                LOG.info("Loading action: {}", action.getClass().getSimpleName());
            }

            ProcessorConfig processorConfig = ProcessorConfig.newProcessConfigBuilder()
                    .aphrodite(aphrodite)
                    .evaluators(evaluatorServices)
                    .actions(actionServices)
                    .parsedStreams(parsedStreams)
                    .writePermittedStreams(writePermittedStreams)
                    .performWriteOperations(performWriteAction)
                    .performReviewAction(performReviewAction)
                    .reportFile(reportFile)
                    .build();

            ServiceLoader<Processor> processorsServices = ServiceLoader.load(Processor.class);
            LOG.info("Loading processor.");
            List<Processor> processors = new ArrayList<>();
            for (Processor processor : processorsServices) {
                LOG.info("Loading processor: {}", processor.getClass().getName());
                processor.init(processorConfig);
                processors.add(processor);
            }
            return processors;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void start() {
//        fetchPullRequestReferences().forEach(this::process);
        fetchSingle().forEach(this::process);
    }

    private List<PullRequestReference> fetchSingle() {
        try {
            String prURI = "https://github.com/wildfly/wildfly/pull/19827";
            String streamDefinitionName = "wildfly";
            String componentDefinitionName = "wildfly-wildlfy";
            PullRequest pullRequest = aphrodite.getPullRequest(URI.create(prURI));
            StreamDefinition streamDefinition = new StreamDefinition(streamDefinitionName);
            streamDefinition.setStream(aphrodite.getStream(streamDefinitionName));
            StreamComponentDefinition componentDefinition = new StreamComponentDefinition(componentDefinitionName, streamDefinition);
            componentDefinition.setStreamComponent(aphrodite.getStream(streamDefinitionName).getComponent(componentDefinitionName));
            PullRequestReference reference = new PullRequestReference(pullRequest, componentDefinition);
            return List.of(reference);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void process(PullRequestReference pullRequestReferences) {
        for (Processor processor : processors) {
            try {
                LOG.info("Executing processors: {} for pull request {}", processor.getClass().getName(), pullRequestReferences);
                processor.process(pullRequestReferences);
            } catch (ProcessorException e) {
                LOG.error("Executing processors", e);
            }
        }
    }

    private List<PullRequestReference> fetchPullRequestReferences() {
        List<PullRequestReference> pullRequests = new ArrayList<>();
        for (StreamDefinition streamDefinition : this.parsedStreams) {
            for (StreamComponentDefinition streamComponentDefinition : streamDefinition.getStreamComponents()) {
                try {
                    Repository repository = aphrodite.getRepository(streamComponentDefinition.getStreamComponent().getRepositoryURI());
                    if (repository == null) {
                        LOG.warn("Did not find repository: {}", streamComponentDefinition.getStreamComponent().getRepositoryURI());
                        continue;
                    }

                    List<PullRequest> componentPullRequests = aphrodite.getPullRequestsByState(repository, PullRequestState.OPEN);
                    // translate it into refs, add to ret val
                    pullRequests.addAll(componentPullRequests.stream().map(p -> {
                        return new PullRequestReference(p, streamComponentDefinition);
                    }).collect(Collectors.toList()));

                } catch (NotFoundException e) {
                    LOG.warn("Did not find repo", e);
                }
            }
        }
        return pullRequests.stream().filter(this::validPullRequest).toList();
    }

    private boolean validPullRequest(PullRequestReference pr) {
        try {
            StreamComponentDefinition scd = pr.getComponentDefinition();
            Codebase definition = scd.getStreamComponent().getCodebase();
            return pr.getPullRequest().getCodebase().isIn(definition);
        } catch (Exception e) {
            LOG.error("Failed at: {}", pr, e);
            return false;
        }
    }

    public static PullProcessorBuilder newPullProcessorBuilder() {
        return new PullProcessorBuilder();
    }

    public static class PullProcessorBuilder {
        public Aphrodite aphrodite;
        private List<StreamDefinition> parsedStreams;
        private List<StreamDefinition> writePermittedStreams;
        private boolean performWriteAction;
        private boolean performReviewAction;
        private String reportFile;

        public PullProcessorBuilder withAphrodite(Aphrodite aphrodite) {
            this.aphrodite = aphrodite;
            return this;
        }

        public PullProcessorBuilder withStreams(List<StreamDefinition> parsedStreams) {
            this.parsedStreams = parsedStreams;
            return this;
        }

        public PullProcessorBuilder withPermitted(List<StreamDefinition> writePermittedStreams) {
            this.writePermittedStreams = writePermittedStreams;
            return this;
        }

        public PullProcessorBuilder withReportFile(String reportFile) {
            this.reportFile = reportFile;
            return this;
        }

        public PullProcessorBuilder withPerformReviewAction(boolean performReviewAction) {
            this.performReviewAction = performReviewAction;
            return this;
        }

        public PullProcessorBuilder withPerformWriteAction(Boolean performWriteAction) {
            this.performWriteAction = performWriteAction;
            return this;
        }

        public PullProcessor build() throws NotFoundException {
            matchStreams(aphrodite, parsedStreams);
            matchStreams(aphrodite, writePermittedStreams);
            return new PullProcessor(this);
        }
    }

}

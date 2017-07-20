package org.jboss.pull.processor.impl.evaluator;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.pull.processor.Evaluator;
import org.jboss.pull.processor.EvaluatorContext;
import org.jboss.pull.processor.data.Attributes;
import org.jboss.pull.processor.data.EvaluatorData;
import org.jboss.pull.processor.data.LinkData;
import org.jboss.set.aphrodite.Aphrodite;
import org.jboss.set.aphrodite.domain.Codebase;
import org.jboss.set.aphrodite.domain.PullRequest;
import org.jboss.set.aphrodite.domain.Repository;
import org.jboss.set.aphrodite.domain.StreamComponent;
import org.jboss.set.aphrodite.spi.NotFoundException;

public class RepositoryEvaluator implements Evaluator {

    private static Logger logger = Logger.getLogger(RepositoryEvaluator.class.getCanonicalName());

    @Override
    public void eval(EvaluatorContext context, EvaluatorData data) {
        Repository repository = context.getRepository();
        Aphrodite aphrodite = context.getAphrodite();
        PullRequest pullRequest = context.getPullRequest();
        Codebase codeBase = pullRequest.getCodebase();
        StreamComponent streamComponent;
        URL url = pullRequest.getRepository().getURL();

        try {
            streamComponent = aphrodite.getComponentBy(url.toURI(), codeBase);
            data.setAttributeValue(Attributes.REPOSITORY, new LinkData(streamComponent.getName(), repository.getURL()));
        } catch (URISyntaxException e) {
            logger.log(Level.WARNING, "Error to convet from URL to URI for value " + url, e);
        } catch (NotFoundException e) {
            logger.log(Level.FINER, "Error to get StreamComponent with the specified repository " + url + " and branch name " + codeBase.getName(), e);
        }
    }
}

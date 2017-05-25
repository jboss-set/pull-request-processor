package org.jboss.pull.processor.impl.evaluator;

import org.jboss.pull.processor.Evaluator;
import org.jboss.pull.processor.EvaluatorContext;
import org.jboss.pull.processor.data.Attributes;
import org.jboss.pull.processor.data.EvaluatorData;
import org.jboss.pull.processor.data.LinkData;
import org.jboss.set.aphrodite.Aphrodite;
import org.jboss.set.aphrodite.domain.PullRequest;
import org.jboss.set.aphrodite.domain.Repository;
import org.jboss.set.aphrodite.domain.StreamComponent;

public class RepositoryEvaluator implements Evaluator {

    @Override
    public void eval(EvaluatorContext context, EvaluatorData data) {
        Repository repository = context.getRepository();
        Aphrodite aphrodite = context.getAphrodite();
        PullRequest pullRequest = context.getPullRequest();
        try {
            StreamComponent streamComponent =  aphrodite.getComponentBy(pullRequest.getRepository().getURL().toURI(), pullRequest.getCodebase());
            data.setAttributeValue(Attributes.REPOSITORY, new LinkData(streamComponent.getName(), repository.getURL()));
        } catch (Exception e) {
            data.setAttributeValue(Attributes.REPOSITORY, new LinkData(pullRequest.getRepository().getURL().getPath(), repository.getURL()));
        }
    }

}

package org.jboss.pull.processor.impl.evaluator;

import org.jboss.pull.processor.Evaluator;
import org.jboss.pull.processor.EvaluatorContext;
import org.jboss.pull.processor.data.Attributes;
import org.jboss.pull.processor.data.EvaluatorData;
import org.jboss.pull.processor.data.LinkData;
import org.jboss.set.aphrodite.Aphrodite;
import org.jboss.set.aphrodite.domain.Patch;
import org.jboss.set.aphrodite.domain.Repository;
import org.jboss.set.aphrodite.domain.StreamComponent;
import org.jboss.set.aphrodite.spi.NotFoundException;

public class RepositoryEvaluator implements Evaluator {

    @Override
    public void eval(EvaluatorContext context, EvaluatorData data) {
        Repository repository = context.getRepository();
        Aphrodite aphrodite = context.getAphrodite();
        Patch patch = context.getPatch();
        try {
            StreamComponent streamComponent =  aphrodite.getComponentBy(patch.getRepository(), patch.getCodebase());
            data.setAttributeValue(Attributes.REPOSITORY, new LinkData(streamComponent.getName(), repository.getURL()));
        } catch (NotFoundException e) {
            data.setAttributeValue(Attributes.REPOSITORY, new LinkData(patch.getRepository().getURL().getPath(), repository.getURL()));
        }
    }

}

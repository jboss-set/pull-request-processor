package org.jboss.pull.processor.rules;

import org.jboss.pull.processor.Common;
import org.jboss.pull.shared.PullHelper;
import org.jboss.pull.shared.connectors.RedhatPullRequest;
import org.jboss.pull.shared.spi.PullEvaluator.Result;

public class BranchLabelRule extends Rule {

    public BranchLabelRule(PullHelper helper) {
        super(helper);
    }

    @Override
    public Result processPullRequest(RedhatPullRequest pullRequest, Result result) {
        String defaultTitle = pullRequest.getTargetBranchTitle();
        Common.addLabel(helper, pullRequest, defaultTitle);

        return result;
    }

}

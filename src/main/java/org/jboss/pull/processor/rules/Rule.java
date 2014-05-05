package org.jboss.pull.processor.rules;

import org.jboss.pull.shared.PullHelper;
import org.jboss.pull.shared.connectors.RedhatPullRequest;
import org.jboss.pull.shared.spi.PullEvaluator.Result;

public abstract class Rule {

    protected PullHelper helper;

    public Rule(PullHelper helper) {
        this.helper = helper;
    }

    public abstract Result processPullRequest(RedhatPullRequest pullRequest, Result result);
}

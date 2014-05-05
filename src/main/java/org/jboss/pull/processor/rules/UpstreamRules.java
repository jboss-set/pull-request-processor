package org.jboss.pull.processor.rules;

import org.jboss.pull.processor.Messages;
import org.jboss.pull.shared.PullHelper;
import org.jboss.pull.shared.connectors.RedhatPullRequest;
import org.jboss.pull.shared.spi.PullEvaluator.Result;

public class UpstreamRules extends Rule {

    public UpstreamRules(PullHelper helper) {
        super(helper);
    }

    public Result processPullRequest(RedhatPullRequest pullRequest, Result result) {
        // Upstream checks
        if (pullRequest.isUpstreamRequired()) {
            if (pullRequest.hasRelatedPullRequestInDescription()) {
                // Do related PR checks
            } else {
                return result.changeResult(false, Messages.MISSING_UPSTREAM);
            }
        } else {
            System.out.println("Upstream not required");
        }

        return result;
    }
}

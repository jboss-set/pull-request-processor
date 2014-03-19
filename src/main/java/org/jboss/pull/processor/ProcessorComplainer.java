package org.jboss.pull.processor;

import java.util.List;

import org.jboss.pull.shared.Util;
import org.jboss.pull.shared.connectors.RedhatPullRequest;
import org.jboss.pull.shared.spi.PullEvaluator.Result;

public class ProcessorComplainer extends Processor {

    public ProcessorComplainer() throws Exception {
    }

    public void run() {
        System.out.println("Starting at: " + Util.getTime());

        try {
            final List<RedhatPullRequest> pullRequests = helper.getOpenPullRequests();

            for (RedhatPullRequest pullRequest : pullRequests) {
                processPullRequest(pullRequest);
            }
        } finally {
            System.out.println("Completed at: " + Util.getTime());
        }
    }

    public void processPullRequest(RedhatPullRequest pullRequest) {
        Result result = new Result();

        System.out.println("ProcessComplainer processing PullRequest: " + pullRequest.getNumber() + " on repository: "
                + pullRequest.getOrganization() + "/" + pullRequest.getRepository());

        result = checkBugs(pullRequest, result);
        result = checkRelatedPullRequests(pullRequest, result);

        complain(pullRequest, result.getDescription());
    }

    private Result checkBugs(RedhatPullRequest pullRequest, Result result) {
        if (!areBugLinksInDescription(pullRequest)) {
            System.out.println("Missing Bugzilla or JIRA link");
            result.setMergeable(false);
            result.addDescription("Missing Bugzilla or JIRA. Please add link to description");
        }
        return result;
    }

    /**
     * Returns true if a BZ or JIRA link is in the description. However, the actual id is not validated.
     * @param pullRequest
     * @return
     */
    private boolean areBugLinksInDescription(RedhatPullRequest pullRequest) {
        if (pullRequest.isBZInDescription() || pullRequest.isJiraInDescription()) {
            return true;
        }
        return false;
    }

    private Result checkRelatedPullRequests(RedhatPullRequest pullRequest, Result result) {
        if (pullRequest.isUpstreamRequired()) {
            if (!doRelatedPullRequestsExist(pullRequest)) {
                System.out.println("Missing Upstream");
                result.setMergeable(false);
                result.addDescription("Missing Upstream. Please add link to description or indicate 'No upstream required'");
            }
        } else {
            System.out.println("Upstream not required");
        }
        return result;

    }

    private boolean doRelatedPullRequestsExist(RedhatPullRequest pullRequest) {
        List<RedhatPullRequest> relatedPullRequests = pullRequest.getRelatedPullRequests();
        if (relatedPullRequests.isEmpty()) {
            return false;
        }
        return true;
    }

}

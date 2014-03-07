package org.jboss.pull.processor;

import java.util.List;

import org.jboss.pull.shared.Util;
import org.jboss.pull.shared.connectors.RedhatPullRequest;
import org.jboss.pull.shared.connectors.bugzilla.Bug;
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
        if( pullRequest.isUpstreamRequired() ){
            result = checkRelatedPullRequests(pullRequest, result);
        }else{
            System.out.println("Upstream not required");
        }

        complain(pullRequest, result.getDescription());
    }

    private Result checkBugs(RedhatPullRequest pullRequest, Result result) {
        List<Bug> bugs = pullRequest.getBugs();
        if (bugs.isEmpty() && pullRequest.isJiraInDescription()) {
            System.out.println("Missing Bugzilla or JIRA link");
            result.setMergeable(false);
            result.addDescription("Missing Bugzilla or JIRA. Please add link to description");
        }
        return result;
    }

    private Result checkRelatedPullRequests(RedhatPullRequest pullRequest, Result result) {
        List<RedhatPullRequest> relatedPullRequests = pullRequest.getRelatedPullRequests();
        if (relatedPullRequests.isEmpty()) {
            System.out.println("Missing Upstream");
            result.setMergeable(false);
            result.addDescription("Missing Upstream. Please add link to description or indicate 'No upstream required'");
        }
        return result;

    }

}

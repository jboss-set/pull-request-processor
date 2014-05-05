package org.jboss.pull.processor.processes.eap6;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.jboss.pull.processor.Common;
import org.jboss.pull.processor.processes.Processor;
import org.jboss.pull.processor.rules.BranchLabelRule;
import org.jboss.pull.processor.rules.BugRules;
import org.jboss.pull.processor.rules.GenericMilestoneRule;
import org.jboss.pull.processor.rules.UpstreamRules;
import org.jboss.pull.shared.PullHelper;
import org.jboss.pull.shared.Util;
import org.jboss.pull.shared.connectors.RedhatPullRequest;
import org.jboss.pull.shared.spi.PullEvaluator.Result;

public class ProcessorEAP6 extends Processor {

    protected Set<String> branches = new HashSet<String>();

    public ProcessorEAP6() throws Exception {
        init();
    }

    public void setHelper(PullHelper helper) {
        this.helper = helper;
        init();
    }

    private void init() {
        String branchesProperty = Util.require(helper.getProperties(), Common.BRANCHES_PROPERTY);
        StringTokenizer tokenizer = new StringTokenizer(branchesProperty, ", ");
        while (tokenizer.hasMoreTokens()) {
            String branch = tokenizer.nextToken();
            branches.add(branch);
        }
    }

    public void run() {
        System.out.println("Starting " + Util.require(helper.getProperties(), "github.organization") + "/"
                + Util.require(helper.getProperties(), "github.repo") + "at: " + org.jboss.pull.shared.Util.getTime());

        try {
            final List<RedhatPullRequest> pullRequests = helper.getOpenPullRequests();

            for (RedhatPullRequest pullRequest : pullRequests) {
                Result result = processPullRequest(pullRequest);

                if (!result.isMergeable()) {
                    Common.complain(pullRequest, result.getDescription());
                } else {
                    System.out.println("No complaints");
                }
            }
        } finally {
            System.out.println("Completed at: " + org.jboss.pull.shared.Util.getTime());
        }
    }

    public Result processPullRequest(RedhatPullRequest pullRequest) {

        Result result = new Result(true);

        if (branches.contains(pullRequest.getTargetBranchTitle())) {
            System.out.println("\nProcessComplainer processing PullRequest '" + pullRequest.getNumber() + "' against branch '"
                    + pullRequest.getTargetBranchTitle() + "'");

            result = new BranchLabelRule(helper).processPullRequest(pullRequest, result);
            result = new GenericMilestoneRule(helper).processPullRequest(pullRequest, result);

            if (pullRequest.getMilestone() != null && pullRequest.getMilestone().getTitle().equals("on hold")) {
                System.out.println("Github milestone 'on hold'. Do nothing.");
                return result;
            }

            result = new BugRules(helper).processPullRequest(pullRequest, result);
            result = new UpstreamRules(helper).processPullRequest(pullRequest, result);

            return result;
        } else {
            System.out.println("\nSkipping PullRequest '" + pullRequest.getNumber() + "' against branch '"
                    + pullRequest.getTargetBranchTitle() + "'");
        }

        return result;
    }
}

package org.jboss.pull.processor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.PullRequest;
import org.jboss.pull.shared.Util;
import org.jboss.pull.shared.connectors.RedhatPullRequest;
import org.jboss.pull.shared.connectors.bugzilla.BZHelper;
import org.jboss.pull.shared.connectors.github.GithubHelper;
import org.jboss.pull.shared.connectors.jira.JiraHelper;
import org.jboss.pull.shared.spi.PullEvaluator.Result;

public class ProcessorEAP6ClosedPR extends ProcessorEAP6 {

    public static void main(String[] argv) throws Exception {
        System.setProperty("dryrun", "true");

        try {
            ProcessorEAP6ClosedPR processor = new ProcessorEAP6ClosedPR();
            processor.run();
            System.exit(0);
        } catch (Exception e) {
            System.err.println(e);
            e.printStackTrace(System.err);
        }

    }

    GithubHelper ghHelper = null;
    Set<String> milestones = new HashSet<String>();

    public ProcessorEAP6ClosedPR() throws Exception {
        ghHelper = new GithubHelper("processor.properties.file", "./processor-eap-6.properties.example");
    }

    public void run() {
        System.out.println("Starting at: " + Util.getTime());

        try {
            List<RedhatPullRequest> redhatPullRequests = getClosedPullRequests();

            for (RedhatPullRequest pullRequest : redhatPullRequests) {
                Result result = processPullRequest(pullRequest);

                if (!result.isMergeable()) {
                    complain(pullRequest, result.getDescription());
                } else {
                    System.out.println("No complaints");
                }
            }

            for (String milestone : milestones) {
                System.out.println(milestone);
            }

        } catch (Exception e) {
            System.out.println("Something went horribly wrong.");
            e.printStackTrace();
        } finally {
            System.out.println("Completed at: " + Util.getTime());
        }
    }

    public Result processPullRequest(RedhatPullRequest pullRequest) {
        Result result = new Result(true);

        result = bugComplaints(pullRequest, result);

        return result;
    }

    int count = 0;

    @Override
    protected void complain(RedhatPullRequest pullRequest, List<String> description) {
        if (!description.contains(ComplaintMessages.MISSING_BUG)) {
            System.out.println("PullRequest " + pullRequest.getNumber() + " Description: " + description);
        }
    }

    @Override
    protected String getBranchRegex(RedhatPullRequest pullRequest) {
        String branch = pullRequest.getTargetBranchTitle();
        String branchRegex = null;
        if (branch.contains("x")) {
            if (branch.length() == 3) {
                branchRegex = branch.replace("x", "[0-9]+");
            } else if (branch.length() == 5) {
                // TODO: Possibly limit regex pattern based on closed github milestones or tags
                branchRegex = branch.replace("x", "[0-9]+");
            }
        }
        return branchRegex;
    }

    @Override
    protected boolean milestoneRule(Milestone milestone) {
        if (milestone == null) {
            return false;
        }
        return true;
    }

    @Override
    protected Milestone findMilestone(String title) {
        List<Milestone> milestones = helper.getGithubMilestones();

        for (Milestone milestone : milestones) {
            if (milestone.getTitle().equals(title)) {
                return milestone;
            }
        }

        this.milestones.add(title);
        return null;
    }

    @Override
    protected void setMilestone(RedhatPullRequest pullRequest, Milestone milestone) {
        if (!DRY_RUN) {
            pullRequest.setMilestone(milestone);
        }
        postComment(pullRequest, "Milestone changed to '" + milestone.getTitle() + "'");
    }

    List<RedhatPullRequest> redhatPullRequests = new ArrayList<RedhatPullRequest>();;
    BZHelper bzHelper = null;
    JiraHelper jiraHelper = null;

    public List<RedhatPullRequest> getClosedPullRequests() throws Exception {
        System.out.println("Starting getPullRequests: " + Util.getTime());
        List<PullRequest> pullRequests = ghHelper.getPullRequests("closed");

        bzHelper = new BZHelper("processor.properties.file", "./processor-eap-6.properties.example");
        jiraHelper = new JiraHelper("processor.properties.file", "./processor-eap-6.properties.example");

        System.out.println("PR size: " + pullRequests.size());

        Thread thread1 = new Thread(new ThreadingExperiment(1, pullRequests.subList(0, 200)));
        Thread thread2 = new Thread(new ThreadingExperiment(2, pullRequests.subList(200, 400)));
        Thread thread3 = new Thread(new ThreadingExperiment(3, pullRequests.subList(400, 600)));
        Thread thread4 = new Thread(new ThreadingExperiment(4, pullRequests.subList(600, 800)));
        Thread thread5 = new Thread(new ThreadingExperiment(5, pullRequests.subList(800, 1000)));
        Thread thread6 = new Thread(new ThreadingExperiment(6, pullRequests.subList(1000, 1147)));

        thread1.start();
        thread2.start();
        thread3.start();
        thread4.start();
        thread5.start();
        thread6.start();

        thread1.join();
        thread2.join();
        thread3.join();
        thread4.join();
        thread5.join();
        thread6.join();

        return redhatPullRequests;
    }

    public class ThreadingExperiment implements Runnable {

        int thread;
        List<PullRequest> pullRequests;

        public ThreadingExperiment(int thread, List<PullRequest> pullRequests) {
            this.thread = thread;
            this.pullRequests = pullRequests;
        }

        @Override
        public void run() {
            for (PullRequest pullRequest : pullRequests) {
                System.out.println("Thread " + thread + " starting pull request " + pullRequest.getNumber() + " conversion: "
                        + Util.getTime());
                RedhatPullRequest pr = new RedhatPullRequest(pullRequest, bzHelper, jiraHelper, ghHelper);
                synchronized (redhatPullRequests) {
                    redhatPullRequests.add(pr);
                }
            }
        }

    }

}

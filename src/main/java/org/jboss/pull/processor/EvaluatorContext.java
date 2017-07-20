package org.jboss.pull.processor;

import java.util.List;

import org.jboss.set.aphrodite.Aphrodite;
import org.jboss.set.aphrodite.domain.Issue;
import org.jboss.set.aphrodite.domain.PullRequest;
import org.jboss.set.aphrodite.domain.Repository;


public class EvaluatorContext {

    private Aphrodite aphrodite;

    private PullRequest pullrequest;

    private List<Issue> issues;

    private List<PullRequest> related;

    private Repository repository;

    private List<String> allowedStreams;

    public EvaluatorContext(Aphrodite aphrodite, Repository repository, PullRequest pullrequest, List<Issue> issues, List<PullRequest> related, List<String> allowedStreams) {
        this.aphrodite = aphrodite;
        this.pullrequest = pullrequest;
        this.issues = issues;
        this.related = related;
        this.repository = repository;
        this.allowedStreams = allowedStreams;
    }

    public Aphrodite getAphrodite() {
        return aphrodite;
    }

    public PullRequest getPullRequest() {
        return this.pullrequest;
    }

    public String getBranch() {
        return this.pullrequest.getCodebase().getName();
    }

    public List<Issue> getIssues() {
        return issues;
    }

    public List<PullRequest> getRelated() {
        return related;
    }

    public Repository getRepository() {
        return repository;
    }

    public List<String> getAllowedStreams() {
        return allowedStreams;
    }
}

package org.jboss.pull.processor;

import java.util.List;

import org.jboss.set.aphrodite.Aphrodite;
import org.jboss.set.aphrodite.domain.Issue;
import org.jboss.set.aphrodite.domain.Patch;
import org.jboss.set.aphrodite.domain.Repository;


public class EvaluatorContext {

    private Aphrodite aphrodite;

    private Patch patch;

    private List<Issue> issues;

    private List<Patch> related;

    private Repository repository;

    private List<String> allowedStreams;

    public EvaluatorContext(Aphrodite aphrodite, Repository repository, Patch patch, List<Issue> issues, List<Patch> related, List<String> allowedStreams) {
        this.aphrodite = aphrodite;
        this.patch = patch;
        this.issues = issues;
        this.related = related;
        this.repository = repository;
        this.allowedStreams = allowedStreams;
    }

    public Aphrodite getAphrodite() {
        return aphrodite;
    }

    public Patch getPatch() {
        return patch;
    }

    public String getBranch() {
        return patch.getCodebase().getName();
    }

    public List<Issue> getIssues() {
        return issues;
    }

    public List<Patch> getRelated() {
        return related;
    }

    public Repository getRepository() {
        return repository;
    }

    public List<String> getAllowedStreams() {
        return allowedStreams;
    }
}

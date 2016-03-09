package org.jboss.pull.processor;

import java.util.List;

import org.jboss.set.aphrodite.Aphrodite;
import org.jboss.set.aphrodite.domain.Issue;
import org.jboss.set.aphrodite.domain.Patch;
import org.jboss.set.aphrodite.domain.Repository;
import org.jboss.set.aphrodite.spi.StreamService;



public class EvaluatorContext {

	private Aphrodite aphrodite;
	
	private StreamService streamService;
	
	private Patch patch;
	
	private List<Issue> issues;
	
	private List<Patch> related;
	
	private Repository repository;
	
	public EvaluatorContext(Aphrodite aphrodite, StreamService streamService, Repository repository, Patch patch, List<Issue> issues, List<Patch> related) {
		this.aphrodite = aphrodite;
		this.streamService = streamService;
		this.patch = patch;
		this.issues = issues;
		this.related = related;
		this.repository = repository;
	}
	
	public Aphrodite getAphrodite() {
		return aphrodite;
	}
	
	public StreamService getStreamService() {
		return streamService;
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
	
}

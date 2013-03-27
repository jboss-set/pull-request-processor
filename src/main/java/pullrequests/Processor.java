/*
 * JBoss, Home of Professional Open Source.
 * Copyright (c) 2013, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package pullrequests;

import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.PullRequestService;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @author <a href="mailto:istudens@redhat.com">Ivo Studensky</a>
 */
public class Processor {
    private static final Pattern merge = Pattern.compile(".*merge\\W+this\\W+please.*", Pattern.CASE_INSENSITIVE);

    private static String GITHUB_ORGANIZATION;
    private static String GITHUB_REPO;
    private static String GITHUB_TOKEN;
    private static String GITHUB_BASE_REF;


    static {
        Properties props;
        try {
            props = Util.loadProperties();

            GITHUB_ORGANIZATION = Util.require(props, "github.organization");
            GITHUB_REPO = Util.require(props, "github.repo");
            GITHUB_TOKEN = Util.get(props, "github.token");
            GITHUB_BASE_REF = Util.get(props, "github.base.ref");

        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }

    public static void main(final String[] args) throws Exception {

        final GitHubClient client = new GitHubClient();
        if (GITHUB_TOKEN != null && GITHUB_TOKEN.length() > 0)
            client.setOAuth2Token(GITHUB_TOKEN);

        final Set<Integer> prsToMerge = new LinkedHashSet<Integer>();
        
        final PullRequestService pullRequestService = new PullRequestService(client);
        final IRepositoryIdProvider repository = RepositoryId.create(GITHUB_ORGANIZATION, GITHUB_REPO);
        final List<PullRequest> pullRequests = pullRequestService.getPullRequests(repository, "open");
        System.err.println("pullRequests: " + pullRequests);
        for (PullRequest pullRequest : pullRequests) {
            System.err.println("pullRequest.getTitle(): " + pullRequest.getTitle());
            System.err.println("pullRequest.getBody(): " + pullRequest.getBody());
            System.err.println("pullRequest.getIssueUrl(): " + pullRequest.getIssueUrl());
            System.err.println("pullRequest.getState(): " + pullRequest.getState());
            System.err.println("pullRequest.isMergeable: " + pullRequest.isMergeable());

            System.err.println("pullRequest.getBase().getLabel(): " + pullRequest.getBase().getLabel());
            System.err.println("pullRequest.getBase().getRef(): " + pullRequest.getBase().getRef());
            System.err.println("pullRequest.getBase().getRepo(): " + pullRequest.getBase().getRepo());
            System.err.println("pullRequest.getBase().getUser().getLogin(): " + pullRequest.getBase().getUser().getLogin());
            System.err.println("pullRequest.getHead().getLabel(): " + pullRequest.getHead().getLabel());
            System.err.println("pullRequest.getHead().getRef(): " + pullRequest.getHead().getRef());
            System.err.println("pullRequest.getHead().getRepo(): " + pullRequest.getHead().getRepo());
            System.err.println("pullRequest.getHead().getUser().getLogin(): " + pullRequest.getHead().getUser().getLogin());

//            if (! pullRequest.isMergeable()) {
//                continue;
//            }

            if (! GITHUB_BASE_REF.equals(pullRequest.getBase().getRef())) {
                continue;
            }

            // check comments
            final IssueService issueService = new IssueService(client);
            final List<Comment> comments = issueService.getComments(repository, pullRequest.getNumber());
            for (final Comment comment : comments) {
                System.err.println("comment.getUser().getLogin(): " + comment.getUser().getLogin());
                System.err.println("comment.getBody(): " + comment.getBody());

                if (merge.matcher(comment.getBody()).matches()) {
                    prsToMerge.add(pullRequest.getNumber());
                }
            }
        }
        
        // print the PR numbers to stdout
        for (int prNumber : prsToMerge) {
            System.out.print(prNumber + " ");
        }
        System.out.println("");
    }
    
    
    

}
package org.jboss.pull.processor;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.PullRequestMarker;
import org.jboss.pull.processor.processes.eap7.ProcessorEAP7;
import org.jboss.pull.shared.PullHelper;
import org.jboss.pull.shared.Util;
import org.jboss.pull.shared.connectors.RedhatPullRequest;
import org.jboss.pull.shared.connectors.bugzilla.BZHelper;
import org.jboss.pull.shared.connectors.common.Issue;
import org.jboss.pull.shared.connectors.github.GithubHelper;
import org.jboss.pull.shared.connectors.jira.JiraHelper;
import org.jboss.pull.shared.spi.PullEvaluator.Result;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Test
public class TestComplaints {

    private String DESCRIPTION_BUILDER = "Testing pattern matching. \n %s \n %s \n";

    private BZHelper setupTargetReleaseBugBZHelper(Issue issue) throws MalformedURLException {
        BZHelper mockBZHelper = mock(BZHelper.class);
        when(mockBZHelper.accepts(new URL("https://bugzilla.redhat.com/show_bug.cgi?id=1"))).thenReturn(true);
        when(mockBZHelper.findIssue(new URL("https://bugzilla.redhat.com/show_bug.cgi?id=1"))).thenReturn(issue);
        return mockBZHelper;
    }

    private RedhatPullRequest setupPullRequest(String description) throws MalformedURLException {
        return setupPullRequest(description, "7.x", BugBuilder.getEmptyBug());
    }

    private RedhatPullRequest setupPullRequest(String description, String pulledAgainst) throws MalformedURLException {
        return setupPullRequest(description, pulledAgainst, BugBuilder.getEmptyBug());
    }

    private RedhatPullRequest setupPullRequest(String description, Issue issue) throws MalformedURLException {
        return setupPullRequest(description, "7.x", issue);
    }

    private RedhatPullRequest setupPullRequest(String description, String pulledAgainst, Issue issue)
            throws MalformedURLException {
        BZHelper mockBZHelper = setupTargetReleaseBugBZHelper(issue);

        JiraHelper mockJiraHelper = mock(JiraHelper.class);

        GithubHelper mockGHHelper = mock(GithubHelper.class);
        when(mockGHHelper.getPullRequest("uselessorg", "jboss-eap", 3)).thenReturn(new PullRequest().setBody(""));
        when(mockGHHelper.getPullRequest("wildfly", "wildfly", 3)).thenReturn(new PullRequest().setBody(""));

        PullRequest pr = new PullRequest();
        pr.setBody(description);
        pr.setUrl("https://github.com/uselessorg/jboss-eap/pull/3");
        pr.setBase(new PullRequestMarker().setRef(pulledAgainst));
        pr.setMilestone(new Milestone().setTitle("7.x"));
        return new RedhatPullRequest(pr, mockBZHelper, mockJiraHelper, mockGHHelper);
    }

    @Test
    public void testNoBug() throws Exception {
        RedhatPullRequest pullRequest = setupPullRequest(String.format(DESCRIPTION_BUILDER, "", ""));

        ProcessorEAP7 complainer = new ProcessorEAP7();
        PullHelper mockPullHelper = mock(PullHelper.class);
        when(mockPullHelper.getProperties()).thenReturn(
                Util.loadProperties("src/test/resources/processor-eap-7.properties.example",
                        "src/test/resources/processor-eap-7.properties.example"));
        when(mockPullHelper.getGithubMilestones()).thenReturn(
                new ArrayList<Milestone>(Arrays.asList(new Milestone[] { new Milestone().setTitle("7.x").setState("Open") })));
        complainer.setHelper(mockPullHelper);

        Result result = complainer.processPullRequest(pullRequest);

        AssertJUnit.assertFalse(result.isMergeable());
        AssertJUnit.assertTrue(result.getDescription().toString(), result.getDescription().contains(Messages.MISSING_BUG));
    }

    @Test
    public void testHasBug() throws Exception {
        RedhatPullRequest pullRequest = setupPullRequest(String.format(DESCRIPTION_BUILDER,
                "https://bugzilla.redhat.com/show_bug.cgi?id=1", ""));

        ProcessorEAP7 complainer = new ProcessorEAP7();
        PullHelper mockPullHelper = mock(PullHelper.class);
        when(mockPullHelper.getProperties()).thenReturn(
                Util.loadProperties("src/test/resources/processor-eap-7.properties.example",
                        "src/test/resources/processor-eap-7.properties.example"));
        when(mockPullHelper.getGithubMilestones()).thenReturn(
                new ArrayList<Milestone>(Arrays.asList(new Milestone[] { new Milestone().setTitle("7.x").setState("Open") })));
        complainer.setHelper(mockPullHelper);

        Result result = complainer.processPullRequest(pullRequest);

        AssertJUnit.assertFalse(String.valueOf(result.isMergeable()), result.isMergeable());
        AssertJUnit.assertFalse(result.getDescription().toString(), result.getDescription().contains(Messages.MISSING_BUG));
    }

    @Test
    public void testNoUpstream() throws Exception {
        RedhatPullRequest pullRequest = setupPullRequest(String.format(DESCRIPTION_BUILDER, "", ""));

        ProcessorEAP7 complainer = new ProcessorEAP7();
        PullHelper mockPullHelper = mock(PullHelper.class);
        when(mockPullHelper.getProperties()).thenReturn(
                Util.loadProperties("src/test/resources/processor-eap-7.properties.example",
                        "src/test/resources/processor-eap-7.properties.example"));
        when(mockPullHelper.getGithubMilestones()).thenReturn(
                new ArrayList<Milestone>(Arrays.asList(new Milestone[] { new Milestone().setTitle("7.x").setState("Open") })));
        complainer.setHelper(mockPullHelper);

        Result result = complainer.processPullRequest(pullRequest);

        AssertJUnit.assertFalse(result.isMergeable());
        AssertJUnit.assertTrue(result.getDescription().toString(), result.getDescription().contains(Messages.MISSING_UPSTREAM));
    }

    @Test
    public void testHasUpstream() throws Exception {
        RedhatPullRequest pullRequest = setupPullRequest(String.format(DESCRIPTION_BUILDER,
                "https://github.com/uselessorg/jboss-eap/pull/3", ""));

        ProcessorEAP7 complainer = new ProcessorEAP7();
        PullHelper mockPullHelper = mock(PullHelper.class);
        when(mockPullHelper.getProperties()).thenReturn(
                Util.loadProperties("src/test/resources/processor-eap-7.properties.example",
                        "src/test/resources/processor-eap-7.properties.example"));
        when(mockPullHelper.getGithubMilestones()).thenReturn(
                new ArrayList<Milestone>(Arrays.asList(new Milestone[] { new Milestone().setTitle("7.x").setState("Open") })));
        complainer.setHelper(mockPullHelper);

        Result result = complainer.processPullRequest(pullRequest);

        AssertJUnit.assertFalse(result.isMergeable());
        AssertJUnit
                .assertFalse(result.getDescription().toString(), result.getDescription().contains(Messages.MISSING_UPSTREAM));
    }

    @Test
    public void testHasUpstreamInternalRepoAbbreviated() throws Exception {
        RedhatPullRequest pullRequest = setupPullRequest(String.format(DESCRIPTION_BUILDER, "#3", ""));

        ProcessorEAP7 complainer = new ProcessorEAP7();
        PullHelper mockPullHelper = mock(PullHelper.class);
        when(mockPullHelper.getProperties()).thenReturn(
                Util.loadProperties("src/test/resources/processor-eap-7.properties.example",
                        "src/test/resources/processor-eap-7.properties.example"));
        when(mockPullHelper.getGithubMilestones()).thenReturn(
                new ArrayList<Milestone>(Arrays.asList(new Milestone[] { new Milestone().setTitle("7.x").setState("Open") })));
        complainer.setHelper(mockPullHelper);

        Result result = complainer.processPullRequest(pullRequest);

        AssertJUnit.assertFalse(result.isMergeable());
        AssertJUnit
                .assertFalse(result.getDescription().toString(), result.getDescription().contains(Messages.MISSING_UPSTREAM));
    }

    @Test
    public void testHasUpstreamExternalRepoAbbreviated() throws Exception {
        RedhatPullRequest pullRequest = setupPullRequest(String.format(DESCRIPTION_BUILDER, "wildfly/wildfly#3", ""));

        ProcessorEAP7 complainer = new ProcessorEAP7();
        PullHelper mockPullHelper = mock(PullHelper.class);
        when(mockPullHelper.getProperties()).thenReturn(
                Util.loadProperties("src/test/resources/processor-eap-7.properties.example",
                        "src/test/resources/processor-eap-7.properties.example"));
        when(mockPullHelper.getGithubMilestones()).thenReturn(
                new ArrayList<Milestone>(Arrays.asList(new Milestone[] { new Milestone().setTitle("7.x").setState("Open") })));
        complainer.setHelper(mockPullHelper);

        Result result = complainer.processPullRequest(pullRequest);

        AssertJUnit.assertFalse(result.isMergeable());
        AssertJUnit
                .assertFalse(result.getDescription().toString(), result.getDescription().contains(Messages.MISSING_UPSTREAM));
    }

    @Test
    public void testHasUpstreamNotRequired() throws Exception {
        RedhatPullRequest pullRequest = setupPullRequest(String.format(DESCRIPTION_BUILDER, "no upstream required", ""));

        AssertJUnit.assertFalse(pullRequest.isUpstreamRequired());

        ProcessorEAP7 complainer = new ProcessorEAP7();
        PullHelper mockPullHelper = mock(PullHelper.class);
        when(mockPullHelper.getProperties()).thenReturn(
                Util.loadProperties("src/test/resources/processor-eap-7.properties.example",
                        "src/test/resources/processor-eap-7.properties.example"));
        when(mockPullHelper.getGithubMilestones()).thenReturn(
                new ArrayList<Milestone>(Arrays.asList(new Milestone[] { new Milestone().setTitle("7.x").setState("Open") })));
        complainer.setHelper(mockPullHelper);

        Result result = complainer.processPullRequest(pullRequest);

        AssertJUnit.assertFalse(result.isMergeable());
        AssertJUnit
                .assertFalse(result.getDescription().toString(), result.getDescription().contains(Messages.MISSING_UPSTREAM));
    }

    // @Test
    // public void testTargetReleaseNoMatch() throws Exception {
    // RedhatPullRequest pullRequest = setupPullRequest(
    // String.format(DESCRIPTION_BUILDER, "https://bugzilla.redhat.com/show_bug.cgi?id=1", ""), "7.1.x");
    //
    // ProcessorEAP7WrongDirection complainer = new ProcessorEAP7WrongDirection();
    // PullHelper mockPullHelper = mock(PullHelper.class);
    // complainer.setHelper(mockPullHelper);
    //
    // Result result = complainer.processPullRequest(pullRequest);
    //
    // AssertJUnit.assertFalse(result.isMergeable());
    // AssertJUnit.assertTrue(result.getDescription().toString(), result.getDescription().contains(Messages.NO_MATCHING_BUG));
    // }
    //
    // @Test
    // public void testTargetReleaseMultipleSet() throws Exception {
    // RedhatPullRequest pullRequest = setupPullRequest(String.format(DESCRIPTION_BUILDER,
    // "https://bugzilla.redhat.com/show_bug.cgi?id=1", ""), BugBuilder.getTargetReleaseBug());
    //
    // ProcessorEAP7WrongDirection complainer = new ProcessorEAP7WrongDirection();
    // PullHelper mockPullHelper = mock(PullHelper.class);
    // when(mockPullHelper.getBranches()).thenReturn(Arrays.asList(new String[] { "7.x", "7.1.x", "7.2.x" }));
    // complainer.setHelper(mockPullHelper);
    //
    // Result result = complainer.processPullRequest(pullRequest);
    //
    // AssertJUnit.assertFalse(result.isMergeable());
    // AssertJUnit.assertTrue(result.getDescription().toString(),
    // result.getDescription().contains(Messages.getMultipleReleases("1")));
    // }
    //
    // @Test
    // public void testMilestoneNotSet() throws Exception {
    // RedhatPullRequest pullRequest = setupPullRequest(String.format(DESCRIPTION_BUILDER,
    // "https://bugzilla.redhat.com/show_bug.cgi?id=1", ""), BugBuilder.getMilestoneNotSetBug());
    //
    // ProcessorEAP7WrongDirection complainer = new ProcessorEAP7WrongDirection();
    // PullHelper mockPullHelper = mock(PullHelper.class);
    // when(mockPullHelper.getBranches()).thenReturn(Arrays.asList(new String[] { "7.x", "7.1.x", "7.2.x" }));
    // complainer.setHelper(mockPullHelper);
    //
    // Result result = complainer.processPullRequest(pullRequest);
    //
    // AssertJUnit.assertFalse(result.isMergeable());
    // AssertJUnit.assertTrue(result.getDescription().toString(),
    // result.getDescription().contains(Messages.getMilestoneNotSet("1")));
    // }

}

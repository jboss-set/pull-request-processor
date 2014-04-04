package org.jboss.pull.processor;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.PullRequestMarker;
import org.jboss.pull.shared.PullHelper;
import org.jboss.pull.shared.connectors.RedhatPullRequest;
import org.jboss.pull.shared.connectors.bugzilla.BZHelper;
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

    private BZHelper setupTargetReleaseBugBZHelper() throws MalformedURLException {
        BZHelper mockBZHelper = mock(BZHelper.class);
        when(mockBZHelper.accepts(new URL("https://bugzilla.redhat.com/show_bug.cgi?id=1"))).thenReturn(true);
        when(mockBZHelper.findIssue(new URL("https://bugzilla.redhat.com/show_bug.cgi?id=1"))).thenReturn(
                BugBuilder.getTargetReleaseBug());
        return mockBZHelper;
    }

    private RedhatPullRequest setupPullRequest(String description) throws MalformedURLException {
        return setupPullRequest(description, "6.x");
    }

    private RedhatPullRequest setupPullRequest(String description, String pulledAgainst) throws MalformedURLException {
        BZHelper mockBZHelper = setupTargetReleaseBugBZHelper();

        JiraHelper mockJiraHelper = mock(JiraHelper.class);

        GithubHelper mockGHHelper = mock(GithubHelper.class);
        when(mockGHHelper.getPullRequest("uselessorg", "jboss-eap", 3)).thenReturn(new PullRequest().setBody(""));

        PullRequest pr = new PullRequest();
        pr.setBody(description);
        pr.setUrl("https://github.com/uselessorg/jboss-eap/pull/3");
        pr.setBase(new PullRequestMarker().setRef(pulledAgainst));
        return new RedhatPullRequest(pr, mockBZHelper, mockJiraHelper, mockGHHelper);
    }

    @Test
    public void testNoBug() throws Exception {
        RedhatPullRequest pullRequest = setupPullRequest(String.format(DESCRIPTION_BUILDER, "", ""));

        ProcessorEAP6 complainer = new ProcessorEAP6();
        PullHelper mockPullHelper = mock(PullHelper.class);
        complainer.setHelper(mockPullHelper);

        Result result = complainer.processPullRequest(pullRequest);

        AssertJUnit.assertFalse(result.isMergeable());
        AssertJUnit.assertTrue(result.getDescription().contains(ComplaintMessages.MISSING_BUG));
    }

    @Test
    public void testHasBug() throws Exception {
        RedhatPullRequest pullRequest = setupPullRequest(String.format(DESCRIPTION_BUILDER,
                "https://bugzilla.redhat.com/show_bug.cgi?id=1", ""));

        ProcessorEAP6 complainer = new ProcessorEAP6();
        PullHelper mockPullHelper = mock(PullHelper.class);
        complainer.setHelper(mockPullHelper);

        Result result = complainer.processPullRequest(pullRequest);

        AssertJUnit.assertFalse(result.isMergeable());
        AssertJUnit.assertFalse(result.getDescription().contains(ComplaintMessages.MISSING_BUG));
    }
    
    @Test
    public void testNoUpstream() throws Exception {
        RedhatPullRequest pullRequest = setupPullRequest(String.format(DESCRIPTION_BUILDER, "", ""));

        ProcessorEAP6 complainer = new ProcessorEAP6();
        PullHelper mockPullHelper = mock(PullHelper.class);
        complainer.setHelper(mockPullHelper);

        Result result = complainer.processPullRequest(pullRequest);

        AssertJUnit.assertFalse(result.isMergeable());
        AssertJUnit.assertTrue(result.getDescription().contains(ComplaintMessages.MISSING_UPSTREAM));
    }

    @Test
    public void testHasUpstream() throws Exception {
        RedhatPullRequest pullRequest = setupPullRequest(String.format(DESCRIPTION_BUILDER,
                "https://github.com/uselessorg/jboss-eap/pull/3", ""));

        ProcessorEAP6 complainer = new ProcessorEAP6();
        PullHelper mockPullHelper = mock(PullHelper.class);
        complainer.setHelper(mockPullHelper);

        Result result = complainer.processPullRequest(pullRequest);

        AssertJUnit.assertFalse(result.isMergeable());
        AssertJUnit.assertFalse(result.getDescription().contains(ComplaintMessages.MISSING_UPSTREAM));
    }

    @Test
    public void testHasUpstreamNotRequired() throws Exception {
        RedhatPullRequest pullRequest = setupPullRequest(String.format(DESCRIPTION_BUILDER, "no upstream required", ""));

        AssertJUnit.assertFalse(pullRequest.isUpstreamRequired());

        ProcessorEAP6 complainer = new ProcessorEAP6();
        PullHelper mockPullHelper = mock(PullHelper.class);
        complainer.setHelper(mockPullHelper);

        Result result = complainer.processPullRequest(pullRequest);

        AssertJUnit.assertFalse(result.isMergeable());
        AssertJUnit.assertFalse(result.getDescription().contains(ComplaintMessages.MISSING_UPSTREAM));
    }

    @Test
    public void testTargetReleaseNoMatch() throws Exception {
        RedhatPullRequest pullRequest = setupPullRequest(
                String.format(DESCRIPTION_BUILDER, "https://bugzilla.redhat.com/show_bug.cgi?id=1", ""), "6.1.x");

        ProcessorEAP6 complainer = new ProcessorEAP6();
        PullHelper mockPullHelper = mock(PullHelper.class);
        complainer.setHelper(mockPullHelper);

        Result result = complainer.processPullRequest(pullRequest);

        AssertJUnit.assertFalse(result.isMergeable());
        AssertJUnit.assertTrue(result.getDescription().contains(ComplaintMessages.NO_MATCHING_BUG));
    }

    @Test
    public void testTargetReleaseMultipleMatch() throws Exception {
        RedhatPullRequest pullRequest = setupPullRequest(String.format(DESCRIPTION_BUILDER,
                "https://bugzilla.redhat.com/show_bug.cgi?id=1", "https://bugzilla.redhat.com/show_bug.cgi?id=1"));

        ProcessorEAP6 complainer = new ProcessorEAP6();
        PullHelper mockPullHelper = mock(PullHelper.class);
        when(mockPullHelper.getBranches()).thenReturn(Arrays.asList(new String[] { "6.x", "6.1.x", "6.2.x" }));
        complainer.setHelper(mockPullHelper);

        Result result = complainer.processPullRequest(pullRequest);

        AssertJUnit.assertFalse(result.isMergeable());
        AssertJUnit.assertTrue(result.getDescription().contains(ComplaintMessages.MULTIPLE_MATCHING_BUGS));
    }

    @Test
    public void testTargetReleaseMultipleSet() throws Exception {
        RedhatPullRequest pullRequest = setupPullRequest(String.format(DESCRIPTION_BUILDER,
                "https://bugzilla.redhat.com/show_bug.cgi?id=1", ""));

        ProcessorEAP6 complainer = new ProcessorEAP6();
        PullHelper mockPullHelper = mock(PullHelper.class);
        when(mockPullHelper.getBranches()).thenReturn(Arrays.asList(new String[] { "6.x", "6.1.x", "6.2.x" }));
        complainer.setHelper(mockPullHelper);

        Result result = complainer.processPullRequest(pullRequest);

        AssertJUnit.assertFalse(result.isMergeable());
        AssertJUnit.assertTrue(result.getDescription().contains(ComplaintMessages.getMultipleReleases("1")));
    }
}

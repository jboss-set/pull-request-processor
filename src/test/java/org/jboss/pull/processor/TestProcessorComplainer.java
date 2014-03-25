package org.jboss.pull.processor;

import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.PullRequestMarker;
import org.jboss.pull.shared.MockPullHelper;
import org.jboss.pull.shared.connectors.RedhatPullRequest;
import org.jboss.pull.shared.spi.PullEvaluator.Result;
import org.jboss.shared.connectors.bugzilla.MockBZHelper;
import org.jboss.shared.connectors.github.MockGithubHelper;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

@Test
public class TestProcessorComplainer {

    @Test
    public void testNoBug() throws Exception {
        PullRequest pr = new PullRequest();
        pr.setBody("Testing Upstream matching.");
        pr.setUrl("https://github.com/uselessorg/jboss-eap/pull/3");
        RedhatPullRequest pullRequest = new RedhatPullRequest(pr, new MockBZHelper(), new MockGithubHelper());

        ProcessorComplainer complainer = new ProcessorComplainer();
        complainer.setHelper(new MockPullHelper("processor.properties.file", "./processor-eap-6.properties.example"));
        Result result = complainer.processPullRequest(pullRequest);

        AssertJUnit.assertFalse(result.isMergeable());
        AssertJUnit.assertTrue(result.getDescription().contains("Missing Bugzilla or JIRA. Please add link to description"));
    }
    
    @Test
    public void testNoUpstream() throws Exception {
        PullRequest pr = new PullRequest();
        pr.setBody("Testing Upstream matching.");
        pr.setUrl("https://github.com/uselessorg/jboss-eap/pull/3");
        RedhatPullRequest pullRequest = new RedhatPullRequest(pr, new MockBZHelper(), new MockGithubHelper());

        ProcessorComplainer complainer = new ProcessorComplainer();
        complainer.setHelper(new MockPullHelper("processor.properties.file", "./processor-eap-6.properties.example"));
        Result result = complainer.processPullRequest(pullRequest);

        AssertJUnit.assertFalse(result.isMergeable());
        AssertJUnit.assertTrue(result.getDescription().contains("Missing Upstream. Please add link to description or indicate 'No upstream required'"));
    }
    
    @Test
    public void testUpstreamNotRequired() throws Exception {
        PullRequest pr = new PullRequest();
        pr.setBody("Testing Upstream matching. no upstream required. ");
        pr.setUrl("https://github.com/uselessorg/jboss-eap/pull/3");
        RedhatPullRequest pullRequest = new RedhatPullRequest(pr, new MockBZHelper(), new MockGithubHelper());

        ProcessorComplainer complainer = new ProcessorComplainer();
        complainer.setHelper(new MockPullHelper("processor.properties.file", "./processor-eap-6.properties.example"));
        Result result = complainer.processPullRequest(pullRequest);

        AssertJUnit.assertFalse(result.isMergeable());
        AssertJUnit.assertFalse(result.getDescription().contains("Missing Upstream. Please add link to description or indicate 'No upstream required'"));
    }
    
    @Test
    public void testTargetReleaseNoMatch() throws Exception{
        PullRequest pr = new PullRequest();
        pr.setBody("Testing Upstream matching. no upstream required. https://bugzilla.redhat.com/show_bug.cgi?id=1 https://bugzilla.redhat.com/show_bug.cgi?id=2 things and stuff");
        pr.setUrl("https://github.com/uselessorg/jboss-eap/pull/3");
        pr.setBase(new PullRequestMarker().setRef("6.x"));
        RedhatPullRequest pullRequest = new RedhatPullRequest(pr, new MockBZHelper(), new MockGithubHelper());

        ProcessorComplainer complainer = new ProcessorComplainer();
        complainer.setHelper(new MockPullHelper("processor.properties.file", "./processor-eap-6.properties.example"));
        Result result = complainer.processPullRequest(pullRequest);
        
        AssertJUnit.assertFalse(result.isMergeable());
        AssertJUnit.assertTrue(result.getDescription().contains("No bug link contains a target_release that matches this branch. Please review bugs in PR description."));         
    }
    
    @Test
    public void testTargetReleaseSingleMatch() throws Exception{
        PullRequest pr = new PullRequest();
        pr.setBody("Testing Upstream matching. no upstream required. https://bugzilla.redhat.com/show_bug.cgi?id=2 things and stuff");
        pr.setUrl("https://github.com/uselessorg/jboss-eap/pull/3");
        pr.setBase(new PullRequestMarker().setRef("6.2.x"));
        RedhatPullRequest pullRequest = new RedhatPullRequest(pr, new MockBZHelper(), new MockGithubHelper());

        ProcessorComplainer complainer = new ProcessorComplainer();
        complainer.setHelper(new MockPullHelper("processor.properties.file", "./processor-eap-6.properties.example"));
        Result result = complainer.processPullRequest(pullRequest);
        
        AssertJUnit.assertFalse(result.isMergeable());
        AssertJUnit.assertFalse(result.getDescription().contains("Multiple bug links contain a target_release that matches this branch. Please review bugs in PR description.")); 
    }
    
    @Test
    public void testTargetReleaseMultipleMatch() throws Exception{
        PullRequest pr = new PullRequest();
        pr.setBody("Testing Upstream matching. no upstream required. https://bugzilla.redhat.com/show_bug.cgi?id=1 https://bugzilla.redhat.com/show_bug.cgi?id=2 things and stuff");
        pr.setUrl("https://github.com/uselessorg/jboss-eap/pull/3");
        pr.setBase(new PullRequestMarker().setRef("6.2.x"));
        RedhatPullRequest pullRequest = new RedhatPullRequest(pr, new MockBZHelper(), new MockGithubHelper());

        ProcessorComplainer complainer = new ProcessorComplainer();
        complainer.setHelper(new MockPullHelper("processor.properties.file", "./processor-eap-6.properties.example"));
        Result result = complainer.processPullRequest(pullRequest);
        
        AssertJUnit.assertFalse(result.isMergeable());
        AssertJUnit.assertTrue(result.getDescription().contains("Multiple bug links contain a target_release that matches this branch. Please review bugs in PR description."));
    }
    
    @Test
    public void testTargetReleaseMultipleSet() throws Exception{
        PullRequest pr = new PullRequest();
        pr.setBody("Testing Upstream matching. no upstream required. https://bugzilla.redhat.com/show_bug.cgi?id=1 things and stuff");
        pr.setUrl("https://github.com/uselessorg/jboss-eap/pull/3");
        pr.setBase(new PullRequestMarker().setRef("6.2.x"));
        RedhatPullRequest pullRequest = new RedhatPullRequest(pr, new MockBZHelper(), new MockGithubHelper());

        ProcessorComplainer complainer = new ProcessorComplainer();
        complainer.setHelper(new MockPullHelper("processor.properties.file", "./processor-eap-6.properties.example"));
        Result result = complainer.processPullRequest(pullRequest);
        
        AssertJUnit.assertFalse(result.isMergeable());
        AssertJUnit.assertTrue(result.getDescription().contains("Bug id '1' contains multiple target_release values. Please divide into separate bugs."));
    }
}

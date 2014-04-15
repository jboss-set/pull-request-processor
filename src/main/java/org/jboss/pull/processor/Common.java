package org.jboss.pull.processor;

import org.eclipse.egit.github.core.Label;
import org.jboss.pull.shared.PullHelper;
import org.jboss.pull.shared.connectors.RedhatPullRequest;

public abstract class Common {

    protected final boolean DRY_RUN;

    protected PullHelper helper;

    public void setHelper(PullHelper helper) {
        this.helper = helper;
    }

    public Common() throws Exception {
        helper = new PullHelper("processor.properties.file", "./processor-eap-6.properties.example");

        // system property "dryrun=true"
        DRY_RUN = Boolean.getBoolean("dryrun");
        if (DRY_RUN) {
            System.out.println("Running in a dry run mode.");
        }
    }

    protected void addLabel(RedhatPullRequest pullRequest, String labelTitle) {
        Label label = helper.getLabel(labelTitle.replace(" ", "+"));
        if (label != null) {
            if (!hasLabel(pullRequest, labelTitle)) {
                if (!DRY_RUN) {
                    pullRequest.addLabel(label);
                }
                System.out.println("Adding label " + labelTitle);
            }
        }
    }

    protected void removeLabel(RedhatPullRequest pullRequest, String labelTitle) {
        Label label = helper.getLabel(labelTitle.replace(" ", "+"));
        if (label != null) {
            if (hasLabel(pullRequest, labelTitle)) {
                if (!DRY_RUN) {
                    pullRequest.removeLabel(label);
                }
                System.out.println("Removing label " + labelTitle);
            }
        }
    }

    protected boolean hasLabel(RedhatPullRequest pullRequest, String title) {
        for (Label label : pullRequest.getGithubLabels()) {
            if (label.getName().equals(title)) {
                return true;
            }
        }
        return false;
    }

}

package org.jboss.pull.processor.rules;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.pull.processor.Messages;
import org.jboss.pull.shared.connectors.RedhatPullRequest;
import org.jboss.pull.shared.connectors.bugzilla.Bug;
import org.jboss.pull.shared.connectors.common.Flag;

public class FlagRules extends Rules {

    public static final String REQUIRED_FLAGS_PROPERTY = "required.flags";

    public static final String PM_ACK = "pm_ack";
    public static final String QA_ACK = "qa_ack";
    public static final String DEVEL_ACK = "devel_ack";

    protected final Set<String> REQUIRED_FLAGS = new HashSet<String>();

    public FlagRules() throws Exception {
        REQUIRED_FLAGS.add(PM_ACK);
        REQUIRED_FLAGS.add(DEVEL_ACK);
        REQUIRED_FLAGS.add(QA_ACK);

        // final String requiredFlags = Util.require(helper.getProperties(), version + "." + REQUIRED_FLAGS_PROPERTY);
        // final StringTokenizer tokenizer = new StringTokenizer(requiredFlags, ", ");
        // while (tokenizer.hasMoreTokens()) {
        // final String requiredFlag = tokenizer.nextToken();
        // REQUIRED_FLAGS.add(requiredFlag);
        // }
    }

    public void processPullRequest(RedhatPullRequest pullRequest, Bug bug) {

        final Set<String> flagsToCheck = new HashSet<String>(this.REQUIRED_FLAGS);

        final List<Flag> flags = bug.getFlags();
        for (Flag flag : flags) {
            if (flagsToCheck.contains(flag.getName())) {
                if (flag.getStatus() == Flag.Status.POSITIVE) {
                    removeLabel(pullRequest, Messages.getNeedsAck(flag.getName()));
                    flagsToCheck.remove(flag.getName());
                }
            }
        }

        if (!flagsToCheck.isEmpty()) {
            for (String flag : flagsToCheck) {
                addLabel(pullRequest, Messages.getNeedsAck(flag));
            }
        } else {
            addLabel(pullRequest, Messages.HAS_ACKS);
        }

    }
}

package org.jboss.pull.processor.impl.evaluator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.pull.processor.Evaluator;
import org.jboss.pull.processor.EvaluatorContext;
import org.jboss.pull.processor.data.Attributes;
import org.jboss.pull.processor.data.EvaluatorData;
import org.jboss.pull.processor.data.LabelData;
import org.jboss.set.aphrodite.domain.Flag;
import org.jboss.set.aphrodite.domain.FlagStatus;
import org.jboss.set.aphrodite.domain.Issue;
import org.jboss.set.aphrodite.domain.Patch;

public class LabelsEvaluator implements Evaluator {

    private static Logger logger = Logger.getLogger("org.jboss.pull");

    @Override
    public void eval(EvaluatorContext context, EvaluatorData data) {

        Patch patch = context.getPatch();
        List<Issue> issues = context.getIssues();

        //  if there aren't any bug related then we show a message
        if(issues.isEmpty()) {
            logger.log(Level.WARNING, "No issues found in patch, " + name() + " not applied to " + patch.getURL());
        }

        Map<String, List<LabelData>> labels = new HashMap<>();
        Map<String, Integer> okays = new HashMap<>();
        data.setAttributeValue(Attributes.LABELS, labels);
        data.setAttributeValue(Attributes.STATUS, okays);

        for(Issue issue : issues) {
            List<LabelData> tmp = new ArrayList<>();
            labels.put(issue.getTrackerId().get(), tmp);
            
            boolean hasAllFlags = true;
            for(Flag flag : Flag.values()) {
                FlagStatus status = issue.getStage().getStatus(flag);
                if(!status.equals(FlagStatus.ACCEPTED)) {
                    hasAllFlags = false;
                    break;
                }
            }
            boolean hasStreams = !Util.getStreams(issue).isEmpty();
            if(hasStreams) {
                okays.put(issue.getTrackerId().get(), hasAllFlags ? 1 : 3);
            } else {
                okays.put(issue.getTrackerId().get(), 2);
            }
            
            tmp.add(new LabelData(context.getBranch(), true));
            tmp.add(new LabelData("Has All Acks", hasAllFlags));
            for(Flag flag : Flag.values()) {
                String label = null;
                switch(flag) {
                    case DEV: label = "Needs devel_ack"; break;
                    case QE: label = "Needs qa_ack"; break;
                    case PM: label = "Needs pm_ack"; break;
                }
                FlagStatus status = issue.getStage().getStatus(flag);
                tmp.add(new LabelData(label, !status.equals(FlagStatus.ACCEPTED)));
            }
        }
    }
}

package org.jboss.pull.processor.impl.action;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.jboss.pull.processor.Action;
import org.jboss.pull.processor.ActionContext;
import org.jboss.pull.processor.Main;
import org.jboss.pull.processor.data.IssueData;
import org.jboss.pull.processor.data.LabelData;
import org.jboss.pull.processor.data.LinkData;
import org.jboss.pull.processor.data.ProcessorData;
import org.jboss.set.aphrodite.domain.Patch;

public class SetLabelsAction implements Action {

    @Override
    public void execute(ActionContext actionContext, List<ProcessorData> processorDataList) {
       for(ProcessorData root : processorDataList) {
           LinkData link = (LinkData) root.getData().get("pullRequest");
           URL pullRequest = link.getLink();
           try {
               Patch patch = actionContext.getAphrodite().getPatch(pullRequest);
               List<IssueData> issues = (List<IssueData>) root.getData().get("issuesRelated");
               Map<String, List<LabelData>> labels = (Map<String, List<LabelData>>) root.getData().get("labels");

               // rearrange labels. mapping from issue - labels to labels.name -> labels
               Map<String, List<LabelData>> labelsRearrange = new HashMap<>();
               for(IssueData issue : issues) {
                   List<LabelData> labelsData = labels.get(issue.getLabel());
                   for(LabelData labelData : labelsData) {
                       List<LabelData> currentData = labelsRearrange.getOrDefault(labelData.getName(),  new ArrayList<LabelData>());
                       labelsRearrange.putIfAbsent(labelData.getName(), currentData);
                       currentData.add(labelData);
                   }
               }

               // consider that the flag is up if all the issues in the same PR are up.
               List<String> added = new ArrayList<>();
               List<String> removed = new ArrayList<>();
               for(Map.Entry<String, List<LabelData>> e : labelsRearrange.entrySet()) {
                   if(e.getValue().stream().filter(j -> !j.isOk()).findAny().isPresent()) {
                       removed.add(e.getKey());
                   } else {
                       added.add(e.getKey());
                   }
               }
               String addedString = added.stream().collect(Collectors.joining(","));
               String removedString = removed.stream().collect(Collectors.joining(","));
               Main.logger.info(patch.getURL() + " labels added [" + addedString + "] removed [" + removedString + "]");
           } catch(Exception e) {
               Main.logger.log(Level.WARNING, "not found something " + pullRequest, e);
           }
       }
    }

}

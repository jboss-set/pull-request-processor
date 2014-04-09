package org.jboss.pull.processor;

import java.util.HashMap;

import org.jboss.pull.shared.connectors.bugzilla.Bug;
import org.jboss.pull.shared.connectors.common.Issue;

public class BugBuilder {
    private static HashMap<String, Object> getEmptyBugMap() {
        HashMap<String, Object> bugMap = new HashMap<String, Object>();
        bugMap.put("id", 1);
        bugMap.put("alias", new String[] {});
        bugMap.put("product", "Jboss Enterprise Application Platform 6");
        bugMap.put("component", new String[] {});
        bugMap.put("version", new String[] {});
        bugMap.put("priority", "");
        bugMap.put("severity", "");
        bugMap.put("target_milestone", "---");
        bugMap.put("target_release", new String[] { "---" });
        bugMap.put("creator", "");
        bugMap.put("assigned_to", "");
        bugMap.put("qa_contact", "");
        bugMap.put("docs_contact", "");
        bugMap.put("status", "NEW");
        bugMap.put("resolution", "");
        bugMap.put("flags", new String[] {});
        bugMap.put("groups", new String[] {});
        bugMap.put("depends_on", new String[] {});
        bugMap.put("blocks", new String[] {});
        bugMap.put("summary", "");
        bugMap.put("description", "");
        return bugMap;
    }

    public static Issue getEmptyBug() {
        return new Bug(getEmptyBugMap());
    }

    public static Issue getTargetReleaseBug() {
        HashMap<String, Object> bugMap = getEmptyBugMap();
        bugMap.put("target_release", new String[] { "6.3.0", "6.2.3" });
        return new Bug(bugMap);
    }
    
    public static Issue getMilestoneNotSetBug() {
        HashMap<String, Object> bugMap = getEmptyBugMap();
        bugMap.put("target_milestone", "---");
        bugMap.put("target_release", new String[] { "6.3.0"});
        return new Bug(bugMap);
    }
    
    public static Issue getMilestoneSetBug() {
        HashMap<String, Object> bugMap = getEmptyBugMap();
        bugMap.put("target_milestone", "ER1");
        bugMap.put("target_release", new String[] { "6.3.0"});
        return new Bug(bugMap);
    }
}

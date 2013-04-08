package org.jboss.pull.processor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jboss.pull.processor.Flag.Status;

public class Bug {

    // Bug Status
    public enum Status {
        NEW,
        ASSIGNED,
        POST,
        MODIFIED,
        ON_DEV,
        ON_QA,
        VERIFIED,
        RELEASE_PENDING,
        CLOSED
    }

    private HashMap<String, Object> bugMap;

    //includes attributes for Bug.get execution
    public static final Object[] include_fields = {"id", "assigned_to", "status", "flags"};

    private int id;
    private String assigned_to;
    private Status status;
    private Set<Flag> flags;

    public Bug(HashMap<String, Object> bugMap) {
        this.bugMap = bugMap;
        initBug();

    }

    @SuppressWarnings("unchecked")
    private void initBug() {
        id = (Integer) bugMap.get("id");
        assigned_to = (String) bugMap.get("assigned_to");
        status = Status.valueOf((String)bugMap.get("status"));
        flags = new HashSet<Flag>();

        Object[] flagObjs = (Object[]) bugMap.get("flags");
        for(Object obj : flagObjs){
            Map<String, Object> flag = (Map<String, Object>)obj;
            String name = (String) flag.get("name");
            String setter = (String) flag.get("setter");
            String s = (String) flag.get("status");
            Flag.Status status;

            if (s.equals(" ")) {
                status = Flag.Status.UNSET;
            } else if (s.equals("?")) {
                status = Flag.Status.UNKNOWN;
            } else if (s.equals("+")) {
                status = Flag.Status.POSITIVE;
            } else if (s.equals("-")) {
                status = Flag.Status.NEGATIVE;
            } else {
                throw new IllegalStateException("Unknown flag state");
            }

            flags.add(new Flag(name, setter, status));
        }
    }

    public HashMap<String, Object> getBugMap() {
        return bugMap;
    }

    public void setBugMap(HashMap<String, Object> bugMap) {
        this.bugMap = bugMap;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAssigned_to() {
        return assigned_to;
    }

    public void setAssigned_to(String assigned_to) {
        this.assigned_to = assigned_to;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Set<Flag> getFlags() {
        return flags;
    }

    public void setFlags(Set<Flag> flags) {
        this.flags = flags;
    }
}

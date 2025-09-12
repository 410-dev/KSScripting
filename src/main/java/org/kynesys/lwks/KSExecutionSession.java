package org.kynesys.lwks;


import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;

@Getter
public class KSExecutionSession {
    private final KSEnvironment environment;
    private final HashMap<Object, Object> complexVariables = new HashMap<>();
    private final ArrayList<String> packagePaths = new ArrayList<>();
    @Setter private boolean isSessionTerminated = false;
    @Setter private Object terminatingValue = null;
    @Setter private Object lastResult;

    public KSExecutionSession(KSEnvironment environment) {
        this.environment = environment;

        // Add default package paths
        packagePaths.add("org.kynesys.ksscripting.commands");
        packagePaths.add("org.kynesys.ksscripting.types");
        packagePaths.add("org.kynesys.ksscripting.graphickit");
    }
    public void setComplexVariable(Object key, Object value) {
        complexVariables.put(key, value);
    }

    public Object getComplexVariable(Object key) {
        return complexVariables.get(key);
    }
}
package org.kynesys.ksscripting.commands;

import org.kynesys.ksscripting.objects.KSScriptingNull;
import org.kynesys.lwks.KSExecutionSession;
import org.kynesys.lwks.KSScriptingExecutable;

public class InspectSession implements KSScriptingExecutable {
    @Override
    public String returnType() {
        return KSScriptingNull.class.getName();
    }

    @Override
    public Object execute(Object[] args, KSExecutionSession session) throws Exception {
        System.out.println("Last Result: " + session.getLastResult().toString());
        System.out.println();
        System.out.println("Current environment variables:");
        for (String key : session.getEnvironment().getEnvVar().keySet()) {
            System.out.println("    " + key + ": " + session.getEnvironment().getEnvVar().get(key));
        }
        System.out.println();
        System.out.println("Current environment Real User:");
        System.out.println("    " + session.getEnvironment().getRealUser());
        System.out.println();
        System.out.println("Current privileged User:");
        System.out.println("    " + session.getEnvironment().getPrivilegedUser());
        System.out.println();
        System.out.println("Current Complex variables:"); // Print type as well
        for (Object key : session.getComplexVariables().keySet()) {
            System.out.println("    " + key + ": " + session.getComplexVariables().get(key).toString() + " (" + session.getComplexVariables().get(key).getClass().getName() + ")");
        }
        return new KSScriptingNull();
    }
}

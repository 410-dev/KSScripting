package org.kynesys.ksscripting.commands;

import org.kynesys.ksscripting.objects.KSScriptingNull;
import org.kynesys.lwks.KSExecutionSession;
import org.kynesys.lwks.KSScriptingExecutable;

public class StoreValue implements KSScriptingExecutable {
    @Override
    public String returnType() {
        return KSScriptingNull.class.getName();
    }

    @Override
    public Object execute(Object[] args, KSExecutionSession session) throws Exception {
        // Usage: StoreValue <variable name> = <value>

        if (args == null || args.length < 3) {
            throw new RuntimeException("StoreValue requires at least 3 arguments: <variable name> = <value>");
        }

        String variableName = (String) args[0];
        Object value = args[2];

        // Treat as string if there are more than 3 arguments
        if (args.length > 3) {
            StringBuilder stringValue = new StringBuilder();
            for (int i = 2; i < args.length; i++) {
                stringValue.append(args[i].toString());
                if (i < args.length - 1) {
                    stringValue.append(" ");
                }
            }
            value = stringValue.toString();
        }

        // Store the value in the session
        session.setComplexVariable(variableName, value);

        // Return null to indicate success
        return new KSScriptingNull();
    }
}

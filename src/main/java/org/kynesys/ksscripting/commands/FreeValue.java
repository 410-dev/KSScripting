package org.kynesys.ksscripting.commands;

import org.kynesys.ksscripting.objects.KSScriptingNull;
import org.kynesys.lwks.KSExecutionSession;
import org.kynesys.lwks.KSScriptingExecutable;

public class FreeValue implements KSScriptingExecutable {
    @Override
    public String returnType() {
        return KSScriptingNull.class.getName();
    }

    @Override
    public Object execute(Object[] args, KSExecutionSession session) throws Exception {
        // Usage: FreeValue <variable name>, <variable name>, ...

        if (args == null || args.length < 1) {
            throw new RuntimeException("FreeValue requires at least 1 arguments: <variable name>");
        }

        // Remove all variables
        for (Object id : args) {
            session.getComplexVariables().remove(id);
        }

        // Return null to indicate success
        return new KSScriptingNull();
    }
}

package org.kynesys.ksscripting.types;


import org.kynesys.lwks.KSExecutionSession;
import org.kynesys.lwks.KSScriptingExecutable;

public class Null implements KSScriptingExecutable {
    @Override
    public String returnType() {
        return Integer.class.getName();
    }

    @Override
    public Object execute(Object[] args, KSExecutionSession session) throws Exception {
        return null;
    }
}

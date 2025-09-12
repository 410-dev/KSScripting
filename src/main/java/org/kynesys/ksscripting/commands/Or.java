package org.kynesys.ksscripting.commands;

import org.kynesys.ksscripting.types.Bool;
import org.kynesys.lwks.KSExecutionSession;
import org.kynesys.lwks.KSScriptingExecutable;

public class Or implements KSScriptingExecutable {
    @Override
    public String returnType() {
        return Bool.class.getName();
    }

    @Override
    public Object execute(Object[] args, KSExecutionSession session) throws Exception {
        // Usage: Or <condition1> <condition2> ...
        if (args == null || args.length < 2) {
            throw new RuntimeException("Or requires at least 2 arguments");
        }
        boolean result = false;
        for (Object arg : args) {
            if (arg instanceof Boolean) {
                result = result || (Boolean) arg;
            } else if (arg instanceof String) {
                result = result || Boolean.parseBoolean((String) arg);
            } else {
                throw new RuntimeException("Or requires boolean or string arguments that can be parsed to boolean");
            }
        }
        return result;
    }
}

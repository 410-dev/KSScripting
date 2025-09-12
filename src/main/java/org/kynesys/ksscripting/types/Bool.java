package org.kynesys.ksscripting.types;

import org.kynesys.lwks.KSExecutionSession;
import org.kynesys.lwks.KSScriptingExecutable;

public class Bool implements KSScriptingExecutable {
    @Override
    public String returnType() {
        return java.lang.Boolean.class.getName();
    }

    @Override
    public Object execute(Object[] args, KSExecutionSession session) throws Exception {
        if (args == null || args.length != 1) {
            throw new RuntimeException("GetAsBoolean requires 1 argument");
        }
        Object arg = args[0];
        if (arg instanceof String) {
            try {
                return java.lang.Boolean.parseBoolean((String) arg);
            } catch (NumberFormatException e) {
                throw new RuntimeException("Invalid boolean format: " + arg + ". Expected 'true' or 'false' but got: " + arg);
            }
        } else {
            if (arg instanceof java.lang.Boolean) {
                return arg;
            } else if (arg instanceof Number) {
                return ((Number) arg).intValue() != 0;
            } else {
                return arg != null;
            }
        }
    }
}

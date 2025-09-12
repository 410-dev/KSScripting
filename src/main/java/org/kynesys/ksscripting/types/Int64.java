package org.kynesys.ksscripting.types;

import org.kynesys.lwks.KSExecutionSession;
import org.kynesys.lwks.KSScriptingExecutable;

public class Int64 implements KSScriptingExecutable {
    @Override
    public String returnType() {
        return Long.class.getName();
    }

    @Override
    public Object execute(Object[] args, KSExecutionSession session) throws Exception {
        if (args == null || args.length != 1) {
            throw new RuntimeException("GetAsInteger64 requires 1 argument");
        }
        Object arg = args[0];
        if (arg instanceof Number) {
            return ((Number) arg).longValue();
        } else if (arg instanceof String) {
            try {
                if (arg.toString().equalsIgnoreCase("max")) {
                    return Long.MAX_VALUE;
                } else if (arg.toString().equalsIgnoreCase("min")) {
                    return Long.MIN_VALUE;
                }
                return Long.parseLong((String) arg);
            } catch (NumberFormatException e) {
                throw new RuntimeException("Invalid integer format: " + arg);
            }
        } else {
            throw new RuntimeException("GetAsInteger64 requires a number or a string that can be parsed to an long");
        }
    }
}
package org.kynesys.ksscripting.types;

import org.kynesys.lwks.KSExecutionSession;
import org.kynesys.lwks.KSScriptingExecutable;

public class Int implements KSScriptingExecutable {
    @Override
    public String returnType() {
        return Integer.class.getName();
    }

    @Override
    public Object execute(Object[] args, KSExecutionSession session) throws Exception {
        if (args == null || args.length != 1) {
            throw new RuntimeException("GetAsInteger requires 1 argument");
        }
        Object arg = args[0];
        if (arg instanceof Number) {
            return ((Number) arg).intValue();
        } else if (arg instanceof String) {
            try {
                if (arg.toString().equalsIgnoreCase("max")) {
                    return Integer.MAX_VALUE;
                } else if (arg.toString().equalsIgnoreCase("min")) {
                    return Integer.MIN_VALUE;
                }
                return Integer.parseInt((String) arg);
            } catch (NumberFormatException e) {
                throw new RuntimeException("Invalid integer format: " + arg);
            }
        } else {
            throw new RuntimeException("GetAsInteger requires a number or a string that can be parsed to an integer");
        }
    }
}

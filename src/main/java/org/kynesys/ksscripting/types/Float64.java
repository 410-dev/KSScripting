package org.kynesys.ksscripting.types;

import org.kynesys.lwks.KSExecutionSession;
import org.kynesys.lwks.KSScriptingExecutable;

public class Float64 implements KSScriptingExecutable {
    @Override
    public String returnType() {
        return Double.class.getName();
    }

    @Override
    public Object execute(Object[] args, KSExecutionSession session) throws Exception {
        if (args == null || args.length != 1) {
            throw new RuntimeException("GetAsFloat64 requires 1 argument");
        }
        Object arg = args[0];
        if (arg instanceof Number) {
            return ((Number) arg).doubleValue();
        } else if (arg instanceof String) {
            try {
                if (arg.toString().equalsIgnoreCase("max")) {
                    return Double.MAX_VALUE;
                } else if (arg.toString().equalsIgnoreCase("min")) {
                    return Double.MIN_VALUE;
                }
                return Double.parseDouble((String) arg);
            } catch (NumberFormatException e) {
                throw new RuntimeException("Invalid float format: " + arg);
            }
        } else {
            throw new RuntimeException("GetAsFloat64 requires a number or a string that can be parsed to a double");
        }
    }
}

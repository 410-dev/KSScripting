package org.kynesys.ksscripting.types;

import org.kynesys.lwks.KSExecutionSession;
import org.kynesys.lwks.KSScriptingExecutable;

public class Float  implements KSScriptingExecutable {
    @Override
    public String returnType() {
        return java.lang.Float.class.getName();
    }

    @Override
    public Object execute(Object[] args, KSExecutionSession session) throws Exception {
        if (args == null || args.length != 1) {
            throw new RuntimeException("GetAsFloat requires 1 argument");
        }
        Object arg = args[0];
        if (arg instanceof Number) {
            return ((Number) arg).floatValue();
        } else if (arg instanceof String) {
            try {
                if (arg.toString().equalsIgnoreCase("max")) {
                    return java.lang.Float.MAX_VALUE;
                } else if (arg.toString().equalsIgnoreCase("min")) {
                    return java.lang.Float.MIN_VALUE;
                }
                return java.lang.Float.parseFloat((String) arg);
            } catch (NumberFormatException e) {
                throw new RuntimeException("Invalid float format: " + arg);
            }
        } else {
            throw new RuntimeException("GetAsFloat requires a number or a string that can be parsed to a float");
        }
    }
}

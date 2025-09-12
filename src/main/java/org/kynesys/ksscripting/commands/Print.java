package org.kynesys.ksscripting.commands;

import org.kynesys.lwks.KSExecutionSession;
import org.kynesys.lwks.KSScriptingExecutable;

public class Print implements KSScriptingExecutable {
    @Override
    public String returnType() {
        return String.class.getName();
    }

    @Override
    public Object execute(Object[] args, KSExecutionSession session) throws Exception {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg instanceof String) {
                sb.append(arg);
            } else if (arg instanceof Number) {
                sb.append(arg);
            } else {
                sb.append(arg.getClass().getName()).append(": ").append(arg);
            }

            if (i < args.length - 1) {
                sb.append(" ");
            }
        }
        String result = sb.toString();
        System.out.println(result);
        return result;
    }
}

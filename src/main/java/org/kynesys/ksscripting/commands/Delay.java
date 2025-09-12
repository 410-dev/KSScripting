package org.kynesys.ksscripting.commands;

import org.kynesys.ksscripting.objects.KSScriptingNull;
import org.kynesys.lwks.KSExecutionSession;
import org.kynesys.lwks.KSScriptingExecutable;

public class Delay implements KSScriptingExecutable {

    @Override
    public String returnType() {
        return KSScriptingNull.class.getName();
    }

    @Override
    public Object execute(Object[] args, KSExecutionSession session) throws Exception {
        // Usage: Delay <milliseconds>
        if (args == null || args.length != 1) {
            throw new RuntimeException("Delay requires 1 argument: <milliseconds>");
        }
        if (args[0] instanceof Number) {
            long delay = ((Number) args[0]).longValue();
            if (delay < 0) {
                throw new RuntimeException("Delay cannot be negative");
            }
            Thread.sleep(delay);
            return new KSScriptingNull();
        } else if (args[0] instanceof String delayString) {
            try {
                long delay = Long.parseLong(delayString);
                if (delay < 0) {
                    throw new RuntimeException("Delay cannot be negative");
                }
                Thread.sleep(delay);
                return new KSScriptingNull();
            } catch (NumberFormatException e) {
                throw new RuntimeException("Invalid delay format. Expected a number or a string that can be parsed to a number.");
            }
        }
        return new KSScriptingNull();
    }
}

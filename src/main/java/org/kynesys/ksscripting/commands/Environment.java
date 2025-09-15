package org.kynesys.ksscripting.commands;

import org.kynesys.ksscripting.objects.KSScriptingNull;
import org.kynesys.lwks.KSExecutionSession;
import org.kynesys.lwks.KSScriptingExecutable;

import java.util.HashMap;

public class Environment implements KSScriptingExecutable {
    @Override
    public String returnType() {
        return KSScriptingNull.class.getName();
    }

    @Override
    public Object execute(Object[] args, KSExecutionSession session) throws Exception {
        // Usage:
        //   Environment list
        //   Environment set <env> <value>

        if (args.length != 1 && args.length != 3) {
            throw new IllegalArgumentException("Required parameter: <action | list, set> [optional: key] [optional: val]");
        }

        if (!(args[0] instanceof String)) {
            throw new IllegalArgumentException("First parameter must be a string");
        }

        if (args.length == 1) {
            if (args[0].equals("list")) {
                return session.getEnvironment().getEnvVar();
            }
            throw new IllegalArgumentException("Unknown action: " + args[0]);
        }

        if (!(args[1] instanceof String)) {
            throw new IllegalArgumentException("Second parameter must be a string");
        }

        if (!(args[2] instanceof String) && !(args[2] == null)) {
            throw new IllegalArgumentException("Third parameter must be either a string or null");
        }

        if (args[2] == null) {
            session.getEnvironment().getEnvVar().remove(args[1]);
        } else {
            session.getEnvironment().getEnvVar().put(args[1].toString(), args[2].toString());
        }

        return new KSScriptingNull();
    }
}

package org.kynesys.ksscripting.types;

import org.kynesys.ksscripting.objects.KSScriptingNull;
import org.kynesys.lwks.KSExecutionSession;
import org.kynesys.lwks.KSScriptingExecutable;

public class TypeOf implements KSScriptingExecutable {
    @Override
    public String returnType() {
        return String.class.getName();
    }

    @Override
    public Object execute(Object[] args, KSExecutionSession session) throws Exception {
        // Usage: TypeOf <object>

        if (args.length != 1) {
            throw new IllegalArgumentException("TypeOf requires exactly one argument.");
        }

        Object arg = args[0];
        if (KSScriptingNull.isNull(arg)) {
            return "null";
        }

        return arg.getClass().getName();
    }
}

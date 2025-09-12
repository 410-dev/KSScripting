package org.kynesys.ksscripting.types;

import org.kynesys.lwks.KSExecutionSession;
import org.kynesys.lwks.KSScriptingExecutable;

import java.util.ArrayList;
import java.util.List;

public class Iterable implements KSScriptingExecutable {
    @Override
    public String returnType() {
        return List.class.getName();
    }

    @Override
    public Object execute(Object[] args, KSExecutionSession session) throws Exception {
        // Usage: Iterable
        // Usage: Iterable <list>
        // Usage: Iterable <delimiter regex> <string>
        if (args == null || args.length > 2) {
            throw new RuntimeException("Iterable requires 2 arguments at most, but got: " + (args == null ? 0 : args.length));
        }

        if (args.length == 1) {
            if (args[0] instanceof List) {
                return args[0];
            } else {
                throw new RuntimeException("Iterable requires a list as the first argument when casting.");
            }
        } else if (args.length == 0) {
            return new ArrayList<>();
        } else {
            if (args[0] instanceof String delimiter && args[1] instanceof String str) {
                return List.of(str.split(delimiter));
            } else {
                throw new RuntimeException("Iterable requires a string as the first argument and a string as the second argument when splitting.");
            }
        }
    }
}

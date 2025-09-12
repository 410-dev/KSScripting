package org.kynesys.ksscripting.types;

import org.kynesys.lwks.KSExecutionSession;
import org.kynesys.lwks.KSScriptingExecutable;

import java.util.HashMap;

public class Map implements KSScriptingExecutable {
    @Override
    public String returnType() {
        return java.util.Map.class.getName();
    }

    @Override
    public Object execute(Object[] args, KSExecutionSession session) throws Exception {
        // Usage: Map <Map>
        //        Map
        //        Map <delimiter> <keys> <values>

        if (args == null || args.length > 3) {
            throw new RuntimeException("Map requires 1 or 3 arguments, but got: " + (args == null ? 0 : args.length));
        }

        if (args.length == 1) {
            if (args[0] instanceof java.util.Map) {
                return args[0];
            } else {
                throw new RuntimeException("Map requires a Map as the first argument");
            }
        } else if (args.length == 0) {
            return new HashMap<>();
        } else if (args.length == 3) {
            String delimiter = (String) args[0];
            String[] keys = ((String) args[1]).split(delimiter);
            String[] values = ((String) args[2]).split(delimiter);
            java.util.Map<String, String> map = new java.util.HashMap<>();
            for (int i = 0; i < keys.length; i++) {
                map.put(keys[i], values[i]);
            }
            return map;
        } else {
            throw new RuntimeException("Invalid number of arguments. Expected 1 or 3 but got: " + args.length);
        }
    }
}

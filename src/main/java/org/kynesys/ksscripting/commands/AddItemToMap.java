package org.kynesys.ksscripting.commands;

import org.kynesys.ksscripting.objects.KSScriptingNull;
import org.kynesys.lwks.KSExecutionSession;
import org.kynesys.lwks.KSScriptingExecutable;

import java.util.Map;

public class AddItemToMap implements KSScriptingExecutable {
    @Override
    public String returnType() {
        return Map.class.getName();
    }

    @Override
    public Object execute(Object[] args, KSExecutionSession session) throws Exception {
        // Usage: AddItemToMap <map> <key> <item>
        if (args.length != 3) {
            throw new IllegalArgumentException("AddItemToMap requires exactly three arguments.");
        }

        Object map = args[0];
        Object key = args[1];
        Object item = args[2];

        if (KSScriptingNull.isNull(map)) {
            throw new IllegalArgumentException("The first argument cannot be null.");
        }

        if (map instanceof Map) {

            // Make a copy of the map to avoid modifying the original
            Map<Object, Object> newMap = ((Map<Object, Object>) map).getClass().newInstance();
            newMap.putAll((Map<Object, Object>) map);

            // Add the new key-value pair to the copied map
            newMap.put(key, item);
            return newMap; // Return the modified map
            
        } else {
            throw new IllegalArgumentException("The first argument must be a Map.");
        }

    }
}

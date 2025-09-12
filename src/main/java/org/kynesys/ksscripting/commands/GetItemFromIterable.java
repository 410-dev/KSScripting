package org.kynesys.ksscripting.commands;

import org.kynesys.lwks.KSExecutionSession;
import org.kynesys.lwks.KSScriptingExecutable;

public class GetItemFromIterable implements KSScriptingExecutable {
    @Override
    public String returnType() {
        return Object.class.getName();
    }

    @Override
    public Object execute(Object[] args, KSExecutionSession session) throws Exception {
        if (args == null || args.length != 2) {
            throw new RuntimeException("GetItemFromIterable requires 2 arguments");
        }

        Object iterable = args[0];
        Object index = args[1];
        switch (iterable) {
            case java.util.List<?> list -> {
                if (index instanceof Number number) {
                    return list.get(number.intValue());
                } else {
                    throw new RuntimeException("GetItemFromIterable requires a number as the second argument");
                }
            }
            case java.util.Collection<?> collection -> {
                if (index instanceof Number number) {
                    return collection.toArray()[number.intValue()];
                } else {
                    throw new RuntimeException("GetItemFromIterable requires a number as the second argument");
                }
            }
            case java.util.Map<?, ?> map -> {
                if (index instanceof String key) {
                    return map.get(key);
                } else {
                    throw new RuntimeException("GetItemFromIterable requires a string as the second argument");
                }
            }
            case Object[] array -> {
                if (index instanceof Number number) {
                    return array[number.intValue()];
                } else {
                    throw new RuntimeException("GetItemFromIterable requires a number as the second argument");
                }
            }
            case null, default ->
                    throw new RuntimeException("GetItemFromIterable requires a List, Collection, Map, or Object[] as the first argument but got " + iterable.getClass().getName());
        }
    }
}

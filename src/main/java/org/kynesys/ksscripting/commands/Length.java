package org.kynesys.ksscripting.commands;

import org.kynesys.lwks.KSExecutionSession;
import org.kynesys.lwks.KSScriptingExecutable;

import java.util.List;

public class Length implements KSScriptingExecutable {
    @Override
    public String returnType() {
        return Integer.class.getName();
    }

    @Override
    public Object execute(Object[] args, KSExecutionSession session) throws Exception {
        if (args == null || args.length != 1) {
            throw new RuntimeException("Length requires 1 argument");
        }
        Object arg = args[0];
        return switch (arg) {
            case String s -> s.length();
            case Object[] objects -> objects.length;
            case int[] ints -> ints.length;
            case long[] longs -> longs.length;
            case double[] doubles -> doubles.length;
            case float[] floats -> floats.length;
            case boolean[] booleans -> booleans.length;
            case byte[] bytes -> bytes.length;
            case short[] shorts -> shorts.length;
            case char[] chars -> chars.length;
            case List<?> objects -> objects.size();
            case java.util.Collection<?> objects -> objects.size();
            case java.util.Map<?, ?> map -> map.size();
            case null, default -> throw new RuntimeException("Length requires any of: String, Object[], int[], long[], double[], float[], boolean[], byte[], short[], char[], List, Collection, Map");
        };
    }
}

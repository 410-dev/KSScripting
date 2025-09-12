package org.kynesys.ksscripting.commands;

import org.kynesys.lwks.KSExecutionSession;
import org.kynesys.lwks.KSScriptingExecutable;

import java.util.ArrayList;
import java.util.List;

public class Range implements KSScriptingExecutable {

    @Override
    public String returnType() {
        return List.class.getName();
    }

    private int getIntValue(Object o) {
        int value = 0;
        if (o instanceof Number) {
            value = ((Number) o).intValue();
        } else if (o instanceof String) {
            try {
                value = Integer.parseInt((String) o);
            } catch (NumberFormatException e) {
                throw new RuntimeException("Range requires a number as the first argument");
            }
        } else {
            throw new RuntimeException("Range requires a number as the first argument");
        }
        return value;
    }

    @Override
    public Object execute(Object[] args, KSExecutionSession session) throws Exception {
        // Usage:
        //   Range <start> <end> <step>
        //   Range <start> <end>
        //   Range <end>

        if (args == null || args.length < 1 || args.length > 3) {
            throw new RuntimeException("Range requires 1 to 3 arguments");
        }

        int start = 0;
        int end;
        int step = 1;
        if (args.length == 1) {
            end = getIntValue(args[0]);
        } else if (args.length == 2) {
            start = getIntValue(args[0]);
            end = getIntValue(args[1]);
        } else {
            start = getIntValue(args[0]);
            end = getIntValue(args[1]);
            step = getIntValue(args[2]);
        }

        if (step == 0) {
            throw new RuntimeException("Step cannot be zero");
        }
        if (step < 0 && start < end) {
            throw new RuntimeException("Step is negative but start is less than end");
        }
        if (step > 0 && start > end) {
            throw new RuntimeException("Step is positive but start is greater than end");
        }

        List<Object> result = new ArrayList<>();
        if (step > 0) {
            for (int i = start; i < end; i += step) {
                result.add(i);
            }
        } else {
            for (int i = start; i > end; i += step) {
                result.add(i);
            }
        }
        return result;
    }
}

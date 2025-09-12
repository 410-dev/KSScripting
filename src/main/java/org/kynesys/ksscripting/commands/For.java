package org.kynesys.ksscripting.commands;

import lombok.Getter;
import org.kynesys.ksscripting.objects.KSScriptingNull;

import org.kynesys.ksscripting.KSScriptingInterpreter;
import org.kynesys.lwks.KSExecutionSession;
import org.kynesys.lwks.KSScriptingExecutable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public class For implements KSScriptingExecutable {

    private final boolean preprocessingInterpreterWhitelistEnabled = true;
    private final int[] preprocessingInterpreterWhitelist = new int[]{2}; // 0: varname, 1: "in", 2: iterable, 3...n: command

    @Override
    public String returnType() {
        return KSScriptingNull.class.getName();
    }

    @Override
    public Object execute(Object[] args, KSExecutionSession session) throws Exception {
        // Usage:
        //  For <varname> in <iterable> <command..>


        if (args == null || args.length < 4) {
            throw new RuntimeException("For requires at least 4 arguments");
        }

        String varName = (String) args[0];
        Object iterable = args[2];
        StringBuilder command = new StringBuilder();
        for (int i = 3; i < args.length; i++) {
            Object arg = args[i];
            if (arg instanceof String) {
                command.append(arg);
            } else if (arg instanceof Number) {
                command.append(arg);
            } else {
                command.append(arg.getClass().getName()).append(": ").append(arg);
            }
            if (i < args.length - 1) {
                command.append(" ");
            }
        }

        // Check if iterable is a valid type
        if (!(iterable instanceof java.util.List<?> || iterable instanceof Object[])) {
            throw new RuntimeException("ForIn requires a List, or Object[] as the second argument");
        }

        // Convert to iterable as list or an array
        List<Object> list = new ArrayList<>();
        if (iterable instanceof List<?> listIterable) {
            list.addAll(listIterable);
        }
        else {
            Object[] array = (Object[]) iterable;
            Collections.addAll(list, array);
        }

        // Execute command for each value in the range
        Object originalValue = session.getComplexVariable(varName);
        for (Object o : list) {
            session.setComplexVariable(varName, o);
            // Execute the command
            KSScriptingInterpreter.executeLine(command.toString(), session);
        }
        // Restore original value
        session.setComplexVariable(varName, originalValue);
        return new KSScriptingNull();
    }
}

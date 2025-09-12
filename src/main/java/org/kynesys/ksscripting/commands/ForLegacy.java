package org.kynesys.ksscripting.commands;

import lombok.Getter;
import org.kynesys.ksscripting.objects.KSScriptingNull;
import org.kynesys.lwks.KSExecutionSession;
import org.kynesys.lwks.KSScriptingExecutable;
import org.kynesys.ksscripting.KSScriptingInterpreter;

@Getter
public class ForLegacy implements KSScriptingExecutable {

    private final boolean preprocessingInterpreterWhitelistEnabled = true;
    private final int[] preprocessingInterpreterWhitelist = new int[]{2, 3, 4}; // 0: varname, 1: "in", 2: start, 3: end, 4: step, 5...n: command

    @Override
    public String returnType() {
        return KSScriptingNull.class.getName();
    }

    @Override
    public Object execute(Object[] args, KSExecutionSession session) throws Exception {
        // Usage:
        //  For <varname> in 0 10 1 <command..>
        //
        // Range is always inclusive...exclusive


        if (args == null || args.length < 6) {
            throw new RuntimeException("For requires at least 6 arguments");
        }

        String varName = (String) args[0];
        String startStr = args[2].toString();
        String endStr = args[3].toString();
        String stepStr = args[4].toString();
        int start = Integer.parseInt(startStr);
        int end = Integer.parseInt(endStr);
        int step = Integer.parseInt(stepStr);

        StringBuilder command = new StringBuilder();
        for (int i = 5; i < args.length; i++) {
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

        // Execute command for each value in the range
        Object originalValue = session.getComplexVariable(varName);
        for (int i = start; i < end; i+=step) {
            session.setComplexVariable(varName, i);
            // Execute the command
            KSScriptingInterpreter.executeLine(command.toString(), session);
        }
        // Restore original value
        session.setComplexVariable(varName, originalValue);
        return new KSScriptingNull();
    }
}

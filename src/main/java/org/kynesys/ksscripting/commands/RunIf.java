package org.kynesys.ksscripting.commands;

import lombok.Getter;
import org.kynesys.lwks.KSExecutionSession;
import org.kynesys.lwks.KSScriptingExecutable;
import org.kynesys.ksscripting.KSScriptingInterpreter;

@Getter
public class RunIf implements KSScriptingExecutable {

    private final boolean preprocessingInterpreterWhitelistEnabled = true;
    private final int[] preprocessingInterpreterWhitelist = new int[]{0}; // 0: condition

    @Override
    public String returnType() {
        return Object.class.getName();
    }

    @Override
    public Object execute(Object[] args, KSExecutionSession session) throws Exception {
        // Usage:
        // RunIf <condition> <command to run if condition is true>

        if (args == null || args.length < 2) {
            throw new RuntimeException("RunIf requires at least 2 arguments");
        }

        Object condition = args[0];
        Object[] commandParts = new Object[args.length - 1];
        System.arraycopy(args, 1, commandParts, 0, args.length - 1);

        // Build line
        StringBuilder line = new StringBuilder();
        for (Object part : commandParts) {
            if (part instanceof String) {
                line.append(part).append(" ");
            } else {
                line.append(part.toString()).append(" ");
            }
        }
//        System.out.println("RunIf: (" + condition + ") " + line.toString());

        if (condition instanceof Boolean) {
            if ((Boolean) condition) {
                // Execute the command
                return KSScriptingInterpreter.executeLine(line.toString(), session);
            } else {
                return null; // Condition is false, do not execute the command
            }
        } else if (condition instanceof String) {
            if (Boolean.parseBoolean((String) condition)) {
                // Execute the command
                return KSScriptingInterpreter.executeLine(line.toString(), session);
            } else {
                return null; // Condition is false, do not execute the command
            }
        } else {
            throw new RuntimeException("RunIf requires a boolean or a string that can be parsed to boolean");
        }
    }
}

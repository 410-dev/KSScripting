package org.kynesys.ksscripting.commands;

import lombok.Getter;
import org.kynesys.ksscripting.objects.KSScriptingNull;
import org.kynesys.lwks.KSExecutionSession;
import org.kynesys.lwks.KSScriptingExecutable;
import org.kynesys.ksscripting.KSScriptingInterpreter;

@Getter
public class While implements KSScriptingExecutable {

    private final boolean preprocessingInterpreterWhitelistEnabled = true;
    private final int[] preprocessingInterpreterWhitelist = new int[]{}; // 0: condition, 1...n: command

    @Override
    public String returnType() {
        return KSScriptingNull.class.getName();
    }

    @Override
    public Object execute(Object[] args, KSExecutionSession session) throws Exception {
        // Usage:
        //  While <condition> <command..>


        if (args == null || args.length < 2) {
            throw new RuntimeException("While requires at least 2 arguments");
        }

        // Build command
        StringBuilder command = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
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


        while (true) {
            // Check condition
            Object condition = args[0];
            if (condition instanceof Boolean) {
                if (!(Boolean) condition) {
                    break; // Condition is false, exit the loop
                }
            } else if (condition instanceof String) {
                try {
                    String conditionLine = condition.toString();
                    // If line starts with { and ends with }, it is a script
                    if (conditionLine.startsWith("{") && conditionLine.endsWith("}")) {
                        conditionLine = conditionLine.substring(1, conditionLine.length() - 1);
                    }
//                    System.out.println("WhileCondition: " + conditionLine);
                    boolean result = (boolean) KSScriptingInterpreter.executeLine(conditionLine, session);
                    if (!result) {
                        break; // Condition is false, exit the loop
                    }
                } catch (Exception e) {
                    throw new RuntimeException("While condition execution failed: " + e.getMessage());
                }
            } else {
                throw new RuntimeException("While requires a boolean or a string that can be parsed to boolean");
            }

            // Execute the command
            try {
                KSScriptingInterpreter.executeLine(command.toString(), session);
            } catch (Exception e) {
                throw new RuntimeException("While command execution failed: " + e.getMessage());
            }
        }

        return new KSScriptingNull();
    }
}

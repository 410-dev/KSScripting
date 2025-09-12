package org.kynesys.ksscripting.commands;

import org.kynesys.ksscripting.types.Bool;
import org.kynesys.lwks.KSExecutionSession;
import org.kynesys.lwks.KSScriptingExecutable;

public class CompareString implements KSScriptingExecutable {
    @Override
    public String returnType() {
        return Bool.class.getName();
    }

    @Override
    public Object execute(Object[] args, KSExecutionSession session) throws Exception {
        // Usage:
        //   CompareString <string1> <operator> <string2>
        //   operator: ==, !=, <, <=, >, >=
        if (args == null || args.length != 3) {
            throw new RuntimeException("CompareString requires 3 arguments: <string1> <operator> <string2>");
        }
        String arg1 = (String) args[0];
        String arg2 = (String) args[2];
        String operator = (String) args[1];

        switch (operator) {
            case "==" -> {
                return arg1.equals(arg2);
            }
            case "!=" -> {
                return !arg1.equals(arg2);
            }
            case "<" -> {
                return arg1.compareTo(arg2) < 0;
            }
            case "<=" -> {
                return arg1.compareTo(arg2) <= 0;
            }
            case ">" -> {
                return arg1.compareTo(arg2) > 0;
            }
            case ">=" -> {
                return arg1.compareTo(arg2) >= 0;
            }
            default -> throw new RuntimeException("Invalid operator. Expected any of ==, !=, <, <=, >, >= but got: " + operator);
        }
    }
}
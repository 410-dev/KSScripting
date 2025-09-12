package org.kynesys.ksscripting.graphickit;

import org.kynesys.lwks.KSExecutionSession;
import org.kynesys.lwks.KSScriptingExecutable;

import java.util.ArrayList;
import java.util.List;

public class GCFilterWindowsByPattern implements KSScriptingExecutable {
    @Override
    public String returnType() {
        return List.class.getName();
    }

    @Override
    public Object execute(Object[] args, KSExecutionSession session) throws Exception {
        // Usage:
        //   GCFilterWindowByPattern <list of windows> <cast to> <invoke method> <(not_) equals | contains | startsWith | endsWith | regex> <pattern>
        // Check if the first argument is a list of windows
        if (args == null || args.length != 5) {
            throw new RuntimeException("GCFilterWindowByPattern requires 5 arguments: <list of windows> <cast to> <invoke method> <(not) equals | contains | startsWith | endsWith | regex> <pattern>");
        }
        if (!(args[0] instanceof List<?> list)) {
            throw new RuntimeException("First argument must be a list of windows.");
        }
        String castTo = (String) args[1];
        String invokeMethod = (String) args[2];
        String condition = (String) args[3];
        String pattern = (String) args[4];

        List<Object> toReturn = new ArrayList<>();

        for (Object window : list) {
            System.out.println("Window: " + window.getClass().getName());
            if (window.getClass().getName().equals(castTo)) {
                try {
                    String value = (String) window.getClass().getMethod(invokeMethod).invoke(window);
                    switch (condition) {
                        case "equals" -> {
                            if (value.equals(pattern)) {
                                toReturn.add(window);
                            }
                        }
                        case "contains" -> {
                            if (value.contains(pattern)) {
                                toReturn.add(window);
                            }
                        }
                        case "startsWith" -> {
                            if (value.startsWith(pattern)) {
                                toReturn.add(window);
                            }
                        }
                        case "endsWith" -> {
                            if (value.endsWith(pattern)) {
                                toReturn.add(window);
                            }
                        }
                        case "regex" -> {
                            if (value.matches(pattern)) {
                                toReturn.add(window);
                            }
                        }
                        case "not_equals" -> {
                            if (!value.equals(pattern)) {
                                toReturn.add(window);
                            }
                        }
                        case "not_contains" -> {
                            if (!value.contains(pattern)) {
                                toReturn.add(window);
                            }
                        }
                        case "not_startsWith" -> {
                            if (!value.startsWith(pattern)) {
                                toReturn.add(window);
                            }
                        }
                        case "not_endsWith" -> {
                            if (!value.endsWith(pattern)) {
                                toReturn.add(window);
                            }
                        }
                        case "not_regex" -> {
                            if (!value.matches(pattern)) {
                                toReturn.add(window);
                            }
                        }
                        default -> {
                            throw new RuntimeException("Invalid condition: " + condition);
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Error invoking method: " + invokeMethod, e);
                }
            }
        }
        if (toReturn.isEmpty()) {
            return new ArrayList<>();
        } else {
            return toReturn;
        }
    }
}

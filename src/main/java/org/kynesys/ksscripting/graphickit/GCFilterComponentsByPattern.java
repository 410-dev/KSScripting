package org.kynesys.ksscripting.graphickit;

import org.kynesys.lwks.KSExecutionSession;
import org.kynesys.lwks.KSScriptingExecutable;

import java.awt.*;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class GCFilterComponentsByPattern implements KSScriptingExecutable {
    @Override
    public String returnType() {
        return List.class.getName();
    }

    private boolean doesMatchCondition(Method matchMethod, Object child, String condition, String pattern) {
        try {
            String value = (String) matchMethod.invoke(child);
            switch (condition) {
                case "equals" -> {
                    if (value.equals(pattern)) {
                        return true;
                    }
                }
                case "contains" -> {
                    if (value.contains(pattern)) {
                        return true;
                    }
                }
                case "startsWith" -> {
                    if (value.startsWith(pattern)) {
                        return true;
                    }
                }
                case "endsWith" -> {
                    if (value.endsWith(pattern)) {
                        return true;
                    }
                }
                case "regex" -> {
                    if (value.matches(pattern)) {
                        return true;
                    }
                }
                case "not_equals" -> {
                    if (!value.equals(pattern)) {
                        return true;
                    }
                }
                case "not_contains" -> {
                    if (!value.contains(pattern)) {
                        return true;
                    }
                }
                case "not_startsWith" -> {
                    if (!value.startsWith(pattern)) {
                        return true;
                    }
                }
                case "not_endsWith" -> {
                    if (!value.endsWith(pattern)) {
                        return true;
                    }
                }
                case "not_regex" -> {
                    if (!value.matches(pattern)) {
                        return true;
                    }
                }
                case "isAny" -> {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private List<Component> traverseComponents(Container component, String invokeMethod, String classStr, String condition, String pattern, int maxDepth, int currentDepth) {
        List<Component> toReturn = new ArrayList<>();

        if (component == null) {
            return toReturn;
        }

        // Recursively traverse the components
        // If condition meets the pattern of the container as well, then add to the list
        for (Component child : component.getComponents()) {

            Class<?> childClass = child.getClass();
            Method[] methods = childClass.getMethods();
            Method matchMethod = null;
            for (Method method : methods) {
                if (method.getName().equals(invokeMethod)) {
                    matchMethod = method;
                    break;
                }
            }

            boolean typeMatches = classStr.equals("any") || childClass.getName().equals(classStr);

            if (child instanceof Container container) {
                if (matchMethod != null) {
                    if (doesMatchCondition(matchMethod, child, condition, pattern) && (typeMatches)) {
                        toReturn.add(child);
                    }
                }
                if (maxDepth == -1 || currentDepth < maxDepth) {
                    // Recursively traverse the child container
                    toReturn.addAll(traverseComponents(container, invokeMethod, classStr, condition, pattern, maxDepth, currentDepth + 1));
                }
            } else {
                if (!typeMatches) {
                    continue;
                }

                try {
                    // Method is not found
                    if (matchMethod == null) continue;

                    if (doesMatchCondition(matchMethod, child, condition, pattern)) {
                        toReturn.add(child);
                    }

                } catch (Exception ignored) {
                    // Ignore the exception
                    // System.out.println("Error invoking method: " + e.getMessage());) {

                }
            }
        }

        return toReturn;
    }

    @Override
    public Object execute(Object[] args, KSExecutionSession session) throws Exception {
        // Usage:
        //   GCFilterComponentByPattern <traverse | notraverse> {Container} {cast to | any} {invoke method} {(not) equals | contains | startsWith | endsWith | regex} {pattern}

        // Check if the first argument is a Container
        if (args == null || args.length != 6) {
            throw new RuntimeException("GCFilterComponentByPattern requires 6 arguments: <traverse | notraverse> {Container} {cast to | any} {invoke method} {(not) equals | contains | startsWith | endsWith | regex | isAny} {pattern} but got " + (args == null ? 0 : args.length) + ".");
        }

        if (!(args[1] instanceof Container container)) {
            throw new RuntimeException("Second argument must be a Container, but got " + args[0].getClass().getName());
        }

        if (!(args[0] instanceof String traverse)) {
            throw new RuntimeException("First argument must be a String, but got " + args[0].getClass().getName());
        } else if (!traverse.equals("traverse") && !traverse.equals("notraverse")) {
            throw new RuntimeException("First argument must be 'traverse' or 'notraverse', but got " + traverse);
        }

        String classStr = (String) args[2];
        String invokeMethod = (String) args[3];
        String condition = (String) args[4];
        String pattern = (String) args[5];

        int maxDepth;
        int currentDepth = 0;
        if (traverse.equals("traverse")) {
            maxDepth = -1;
        } else {
            maxDepth = 0;
        }

        List<Component> toReturn = traverseComponents(container, invokeMethod, classStr, condition, pattern, maxDepth, currentDepth);

        return toReturn;
    }
}

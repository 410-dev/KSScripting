package org.kynesys.ksscripting.commands;

import org.kynesys.lwks.KSExecutionSession;
import org.kynesys.lwks.KSScriptingExecutable;

public class Random implements KSScriptingExecutable {
    @Override
    public String returnType() {
        return Object.class.getName();
    }

    @Override
    public Object execute(Object[] args, KSExecutionSession session) throws Exception {
        // Usage:
        //   Random string {length as int} <regex, default as ".*">
        //   Random int {min as int} {max as int}
        //   Random float {min as float} {max as float}
        //   Random double {min as double} {max as double}
        if (args == null || args.length < 2) {
            throw new RuntimeException("Random requires at least 2 arguments");
        }

        String type = (String) args[0];
        switch (type) {
            case "string" -> {
                if (args.length != 3) {
                    throw new RuntimeException("Random string requires 2 arguments");
                }
                int length = (int) args[1];
                String regex = (String) args[2];
                return generateRandomString(length, regex);
            }
            case "int" -> {
                if (args.length != 3) {
                    throw new RuntimeException("Random int requires 2 arguments");
                }
                int min = (int) args[1];
                int max = (int) args[2];
                return generateRandomInt(min, max);
            }
            case "float" -> {
                if (args.length != 3) {
                    throw new RuntimeException("Random float requires 2 arguments");
                }
                float min = (float) args[1];
                float max = (float) args[2];
                return generateRandomFloat(min, max);
            }
            case "double" -> {
                if (args.length != 3) {
                    throw new RuntimeException("Random double requires 2 arguments");
                }
                double min = (double) args[1];
                double max = (double) args[2];
                return generateRandomDouble(min, max);
            }
            default -> throw new RuntimeException("Unknown random type: " + type);
        }
    }

    private String generateRandomString(int length, String regex) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            char c;
            do {
                c = (char) ((int) (java.lang.Math.random() * 128));
            } while (!String.valueOf(c).matches(regex));
            sb.append(c);
        }
        return sb.toString();
    }

    private int generateRandomInt(int min, int max) {
        return (int) (java.lang.Math.random() * (max - min + 1)) + min;
    }

    private float generateRandomFloat(float min, float max) {
        return (float) (java.lang.Math.random() * (max - min)) + min;
    }

    private double generateRandomDouble(double min, double max) {
        return (java.lang.Math.random() * (max - min)) + min;
    }
}

package org.kynesys.ksscripting.commands;

import org.kynesys.ksscripting.types.Float;
import org.kynesys.lwks.KSExecutionSession;
import org.kynesys.lwks.KSScriptingExecutable;

public class Math implements KSScriptingExecutable {
    @Override
    public String returnType() {
        return Number.class.getName();
    }

    @Override
    public Object execute(Object[] args, KSExecutionSession session) throws Exception {

        // Add all numbers together
        if (args == null || args.length < 3) {
            throw new RuntimeException("Math requires at least 3 arguments: <operation: add, subtract, multiply, divide, mod, pow, sqrt> <number1> <number2> ...");
        }

        // Get operation
        // Operation is any of: add, subtract, multiply, divide, mod, pow, sqrt
        String operation = (String) args[0];

        // Check precision rank, where 0 is int, 1 is long, 2 is float, 3 is double, 4 is BigDecimal
        Object[] numbers = new Object[args.length - 1];
        System.arraycopy(args, 1, numbers, 0, args.length - 1);
        int precisionRank = getPrecisionRank(numbers);

        // Add all numbers together
        Number result = null;
        switch (operation) {
            case "add": {
                result = (Number) args[1];
                for (int i = 2; i < args.length; i++) {
                    result = add(result, args[i], precisionRank);
                }
                break;
            }
            case "subtract": {
                result = (Number) args[1];
                for (int i = 2; i < args.length; i++) {
                    result = subtract(result, args[i], precisionRank);
                }
                break;
            }
            case "multiply": {
                result = (Number) args[1];
                for (int i = 2; i < args.length; i++) {
                    result = multiply(result, args[i], precisionRank);
                }
                break;
            }
            case "divide": {
                result = (Number) args[1];
                for (int i = 2; i < args.length; i++) {
                    result = divide(result, args[i], precisionRank);
                }
                break;
            }
            case "mod": {
                result = (Number) args[1];
                for (int i = 2; i < args.length; i++) {
                    result = mod(result, args[i], precisionRank);
                }
                break;
            }
            case "pow": {
                result = (Number) args[1];
                for (int i = 2; i < args.length; i++) {
                    result = pow(result, args[i], precisionRank);
                }
                break;
            }
            case "sqrt": {
                result = (Number) args[1];
                for (int i = 2; i < args.length; i++) {
                    result = sqrt(result, args[i], precisionRank);
                }
                break;
            }
            default: {
                throw new RuntimeException("Invalid operation: " + operation);
            }
        }
        // Return result
        return result;
    }

    public static Number add(Number a, Object b, int precisionRank) {
        if (precisionRank == 0) {
            return a.intValue() + ((Number) b).intValue();
        } else if (precisionRank == 1) {
            return a.longValue() + ((Number) b).longValue();
        } else if (precisionRank == 2) {
            return a.floatValue() + ((Number) b).floatValue();
        } else if (precisionRank == 3) {
            return a.doubleValue() + ((Number) b).doubleValue();
        } else if (precisionRank == 4) {
            return ((java.math.BigDecimal) a).add((java.math.BigDecimal) b);
        } else {
            throw new RuntimeException("Invalid precision rank: " + precisionRank);
        }
    }

    public static Number subtract(Number a, Object b, int precisionRank) {
        if (precisionRank == 0) {
            return a.intValue() - ((Number) b).intValue();
        } else if (precisionRank == 1) {
            return a.longValue() - ((Number) b).longValue();
        } else if (precisionRank == 2) {
            return a.floatValue() - ((Number) b).floatValue();
        } else if (precisionRank == 3) {
            return a.doubleValue() - ((Number) b).doubleValue();
        } else if (precisionRank == 4) {
            return ((java.math.BigDecimal) a).subtract((java.math.BigDecimal) b);
        } else {
            throw new RuntimeException("Invalid precision rank: " + precisionRank);
        }
    }

    public static Number multiply(Number a, Object b, int precisionRank) {
        if (precisionRank == 0) {
            return a.intValue() * ((Number) b).intValue();
        } else if (precisionRank == 1) {
            return a.longValue() * ((Number) b).longValue();
        } else if (precisionRank == 2) {
            return a.floatValue() * ((Number) b).floatValue();
        } else if (precisionRank == 3) {
            return a.doubleValue() * ((Number) b).doubleValue();
        } else if (precisionRank == 4) {
            return ((java.math.BigDecimal) a).multiply((java.math.BigDecimal) b);
        } else {
            throw new RuntimeException("Invalid precision rank: " + precisionRank);
        }
    }

    public static Number divide(Number a, Object b, int precisionRank) {
        if (precisionRank == 0) {
            return a.intValue() / ((Number) b).intValue();
        } else if (precisionRank == 1) {
            return a.longValue() / ((Number) b).longValue();
        } else if (precisionRank == 2) {
            return a.floatValue() / ((Number) b).floatValue();
        } else if (precisionRank == 3) {
            return a.doubleValue() / ((Number) b).doubleValue();
        } else if (precisionRank == 4) {
            return ((java.math.BigDecimal) a).divide((java.math.BigDecimal) b);
        } else {
            throw new RuntimeException("Invalid precision rank: " + precisionRank);
        }
    }

    public static Number mod(Number a, Object b, int precisionRank) {
        if (precisionRank == 0) {
            return a.intValue() % ((Number) b).intValue();
        } else if (precisionRank == 1) {
            return a.longValue() % ((Number) b).longValue();
        } else if (precisionRank == 2) {
            return a.floatValue() % ((Number) b).floatValue();
        } else if (precisionRank == 3) {
            return a.doubleValue() % ((Number) b).doubleValue();
        } else if (precisionRank == 4) {
            return ((java.math.BigDecimal) a).remainder((java.math.BigDecimal) b);
        } else {
            throw new RuntimeException("Invalid precision rank: " + precisionRank);
        }
    }

    public static Number pow(Number a, Object b, int precisionRank) {
        if (precisionRank == 0) {
            return java.lang.Math.pow(a.intValue(), ((Number) b).intValue());
        } else if (precisionRank == 1) {
            return java.lang.Math.pow(a.longValue(), ((Number) b).longValue());
        } else if (precisionRank == 2) {
            return java.lang.Math.pow(a.floatValue(), ((Number) b).floatValue());
        } else if (precisionRank == 3) {
            return java.lang.Math.pow(a.doubleValue(), ((Number) b).doubleValue());
        } else if (precisionRank == 4) {
            return ((java.math.BigDecimal) a).pow(((java.math.BigDecimal) b).intValue());
        } else {
            throw new RuntimeException("Invalid precision rank: " + precisionRank);
        }
    }

    public static Number sqrt(Number a, Object b, int precisionRank) {
        if (precisionRank == 0) {
            return java.lang.Math.sqrt(a.intValue());
        } else if (precisionRank == 1) {
            return java.lang.Math.sqrt(a.longValue());
        } else if (precisionRank == 2) {
            return java.lang.Math.sqrt(a.floatValue());
        } else if (precisionRank == 3) {
            return java.lang.Math.sqrt(a.doubleValue());
        } else if (precisionRank == 4) {
            return ((java.math.BigDecimal) a).sqrt(new java.math.MathContext(precisionRank));
        } else {
            throw new RuntimeException("Invalid precision rank: " + precisionRank);
        }
    }

    public static int getPrecisionRank(Object[] args) {
        int precisionRank = 0;
        for (Object arg : args) {
            if (arg instanceof Integer) {
                precisionRank = java.lang.Math.max(precisionRank, 0);
            } else if (arg instanceof Long) {
                precisionRank = java.lang.Math.max(precisionRank, 1);
            } else if (arg instanceof Float) {
                precisionRank = java.lang.Math.max(precisionRank, 2);
            } else if (arg instanceof Double) {
                precisionRank = java.lang.Math.max(precisionRank, 3);
            } else if (arg instanceof java.math.BigDecimal) {
                precisionRank = java.lang.Math.max(precisionRank, 4);
            } else {
                throw new RuntimeException("Math operation requires all arguments to be numbers or BigDecimal, but got " + arg.getClass().getSimpleName());
            }
        }
        return precisionRank;
    }
}

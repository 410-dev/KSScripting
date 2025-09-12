package org.kynesys.ksscripting.commands;

import org.kynesys.lwks.KSExecutionSession;
import org.kynesys.lwks.KSScriptingExecutable;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public class InvokeMethodOf implements KSScriptingExecutable {

    private static final Map<Class<?>, Class<?>> PRIMITIVE_TO_WRAPPER = new HashMap<>();
    private static final Map<Class<?>, Class<?>> WRAPPER_TO_PRIMITIVE = new HashMap<>();
    static {
        PRIMITIVE_TO_WRAPPER.put(boolean.class, Boolean.class);
        PRIMITIVE_TO_WRAPPER.put(byte.class, Byte.class);
        PRIMITIVE_TO_WRAPPER.put(short.class, Short.class);
        PRIMITIVE_TO_WRAPPER.put(char.class, Character.class);
        PRIMITIVE_TO_WRAPPER.put(int.class, Integer.class);
        PRIMITIVE_TO_WRAPPER.put(long.class, Long.class);
        PRIMITIVE_TO_WRAPPER.put(float.class, Float.class);
        PRIMITIVE_TO_WRAPPER.put(double.class, Double.class);

        for (Map.Entry<Class<?>, Class<?>> e : PRIMITIVE_TO_WRAPPER.entrySet()) {
            WRAPPER_TO_PRIMITIVE.put(e.getValue(), e.getKey());
        }
    }

    @Override
    public String returnType() {
        return Object.class.getName();
    }

    @Override
    public Object execute(Object[] args, KSExecutionSession session) throws Exception {
        // Usage:
        //   InvokeMethodOf {Object} <method name> <args...>
        if (args == null || args.length < 2) {
            throw new RuntimeException("InvokeMethodOf requires at least 2 arguments: {Object} <method name> <args...>");
        }

        Object target = args[0];
        if (target == null) throw new RuntimeException("Target object is null.");
        String methodName = (String) args[1];

        Object[] callArgs = new Object[args.length - 2];
        System.arraycopy(args, 2, callArgs, 0, args.length - 2);

        MethodSelection sel = selectBestMethod(target.getClass(), methodName, callArgs);
        if (sel == null) {
            throw new RuntimeException("No compatible overload found for " + methodName + " in " + target.getClass().getName());
        }

        Object[] finalArgs = sel.prepareArguments(callArgs);
        try {
            if (!sel.method.canAccess(target)) sel.method.setAccessible(true);
            return sel.method.invoke(target, finalArgs);
        } catch (IllegalAccessException | java.lang.reflect.InvocationTargetException e) {
            throw new RuntimeException("Failed to invoke " + signature(sel.method) + " on " + target.getClass().getName(), e);
        }
    }

    // ---- Method selection ----

    private static MethodSelection selectBestMethod(Class<?> clazz, String name, Object[] args) {
        List<MethodSelection> candidates = new ArrayList<>();
        for (Method m : clazz.getMethods()) {
            if (!m.getName().equals(name)) continue;
            if (!Modifier.isPublic(m.getModifiers())) continue; // keep public surface
            Score score = compatibleScore(m.getParameterTypes(), m.isVarArgs(), args);
            if (score != null) {
                candidates.add(new MethodSelection(m, score));
            }
        }
        if (candidates.isEmpty()) return null;

        // Pick the “best” by score, then by specificity (subclass params win), then by non-varargs preference.
        candidates.sort((a, b) -> {
            int cmp = a.score.compareTo(b.score);
            if (cmp != 0) return cmp;
            // Prefer non-varargs when scores equal
            if (a.method.isVarArgs() != b.method.isVarArgs()) {
                return a.method.isVarArgs() ? 1 : -1;
            }
            // Prefer more specific parameter types
            return compareSpecificity(a.method.getParameterTypes(), b.method.getParameterTypes());
        });

        return candidates.get(0);
    }

    private static int compareSpecificity(Class<?>[] a, Class<?>[] b) {
        int n = java.lang.Math.min(a.length, b.length);
        int score = 0;
        for (int i = 0; i < n; i++) {
            if (a[i] == b[i]) continue;
            if (a[i].isAssignableFrom(b[i])) score += 1;
            else if (b[i].isAssignableFrom(a[i])) score -= 1;
        }
        return score;
    }

    // ---- Compatibility + scoring ----

    private static final class Score implements Comparable<Score> {
        // Lower is better. We accumulate penalties for conversions:
        // 0 = exact
        // 1 = boxing/unboxing
        // 2 = primitive widening
        // 3 = reference upcast
        // +1 if varargs packing used
        final int value;
        final boolean usedVarargs;
        Score(int value, boolean usedVarargs) { this.value = value; this.usedVarargs = usedVarargs; }

        @Override public int compareTo(Score o) {
            int c = Integer.compare(this.value, o.value);
            if (c != 0) return c;
            // prefer non-varargs if tie
            return Boolean.compare(this.usedVarargs, o.usedVarargs);
        }
    }

    private static Score compatibleScore(Class<?>[] params, boolean isVarArgs, Object[] args) {
        if (!isVarArgs) {
            if (params.length != args.length) return null;
            int acc = 0;
            for (int i = 0; i < params.length; i++) {
                int s = singleParamScore(params[i], (args[i] == null) ? null : args[i].getClass());
                if (s < 0) return null;
                acc += s;
            }
            return new Score(acc, false);
        } else {
            if (args.length < params.length - 1) return null; // not enough for fixed part
            int acc = 0;
            // fixed part
            for (int i = 0; i < params.length - 1; i++) {
                int s = singleParamScore(params[i], (args[i] == null) ? null : args[i].getClass());
                if (s < 0) return null;
                acc += s;
            }
            // varargs part
            Class<?> varArrayType = params[params.length - 1];
            Class<?> compType = varArrayType.getComponentType();
            if (args.length == params.length && args[args.length - 1] != null
                    && varArrayType.isInstance(args[args.length - 1])) {
                // caller already passed an array; accept as is
                return new Score(acc, true);
            } else {
                for (int i = params.length - 1; i < args.length; i++) {
                    int s = singleParamScore(compType, (args[i] == null) ? null : args[i].getClass());
                    if (s < 0) return null;
                    acc += s;
                }
                return new Score(acc + 1, true); // slight penalty for packing
            }
        }
    }

    // returns -1 if incompatible; otherwise a non-negative penalty score
    private static int singleParamScore(Class<?> paramType, Class<?> argType) {
        if (argType == null) return paramType.isPrimitive() ? -1 : 3; // null to primitive impossible; null to ref is upcast-like

        if (paramType.equals(argType)) return 0; // exact

        if (paramType.isAssignableFrom(argType)) return 3; // reference upcast

        // boxing/unboxing match
        if (paramType.isPrimitive()) {
            Class<?> wrapper = PRIMITIVE_TO_WRAPPER.get(paramType);
            if (wrapper != null && wrapper.equals(argType)) return 1;
            // primitive widening after unboxing
            Class<?> argPrim = WRAPPER_TO_PRIMITIVE.getOrDefault(argType, argType.isPrimitive() ? argType : null);
            if (argPrim != null && isWideningPrimitiveConvertible(argPrim, paramType)) return 2;
            return -1;
        } else {
            // param is reference
            Class<?> paramPrim = WRAPPER_TO_PRIMITIVE.get(paramType);
            if (paramPrim != null && argType.isPrimitive() && paramPrim.equals(argType)) return 1; // unboxing in reverse case
            // also allow wrapper-to-wrapper upcasts (e.g., Number <- Integer)
            if (paramType.isAssignableFrom(argType)) return 3;
            return -1;
        }
    }

    private static boolean isWideningPrimitiveConvertible(Class<?> from, Class<?> to) {
        if (!from.isPrimitive() || !to.isPrimitive()) return false;
        if (from == to) return true;
        if (from == byte.class)   return to == short.class || to == int.class || to == long.class || to == float.class || to == double.class;
        if (from == short.class)  return to == int.class || to == long.class || to == float.class || to == double.class;
        if (from == char.class)   return to == int.class || to == long.class || to == float.class || to == double.class;
        if (from == int.class)    return to == long.class || to == float.class || to == double.class;
        if (from == long.class)   return to == float.class || to == double.class;
        if (from == float.class)  return to == double.class;
        if (from == boolean.class) return false;
        return false;
    }

    // ---- Preparing final arguments (boxing/unboxing/widening + varargs packing) ----

    private static Object convert(Object value, Class<?> targetType) {
        if (value == null) return null;

        Class<?> srcType = value.getClass();

        // If already assignable, done.
        if (targetType.isInstance(value)) return value;

        // Handle primitive targets via wrappers
        if (targetType.isPrimitive()) {
            Class<?> wrapper = PRIMITIVE_TO_WRAPPER.get(targetType);
            if (wrapper != null && wrapper.isInstance(value)) {
                // wrapper matches; Method.invoke will unbox. No change needed.
                return value;
            }
            // Try numeric/char conversions
            return convertToPrimitiveCompatible(value, targetType);
        }

        // Reference target but value is primitive wrapper that can be assigned (e.g., Number <- Integer)
        if (targetType.isAssignableFrom(srcType)) {
            return value;
        }

        // No general-purpose coercions beyond numeric/char/boolean are safe
        return value; // let Method.invoke throw if truly incompatible
    }

    private static Object convertToPrimitiveCompatible(Object value, Class<?> primitiveTarget) {
        if (primitiveTarget == boolean.class) {
            if (value instanceof Boolean) return value;
            throw new IllegalArgumentException("Cannot convert " + value.getClass() + " to boolean");
        }
        if (primitiveTarget == char.class) {
            if (value instanceof Character) return value;
            if (value instanceof Number) {
                int i = ((Number) value).intValue();
                return (char) i;
            }
            if (value instanceof String s && s.length() == 1) {
                return s.charAt(0);
            }
            throw new IllegalArgumentException("Cannot convert " + value.getClass() + " to char");
        }
        if (!(value instanceof Number)) {
            // allow Character -> numeric
            if (value instanceof Character ch) {
                int i = (int) ch.charValue();
                return coerceNumberToPrimitive(i, primitiveTarget);
            }
            throw new IllegalArgumentException("Cannot convert non-number " + value.getClass() + " to numeric primitive");
        }
        Number n = (Number) value;
        return coerceNumberToPrimitive(n, primitiveTarget);
    }

    private static Object coerceNumberToPrimitive(Number n, Class<?> primitiveTarget) {
        if (primitiveTarget == byte.class)   return n.byteValue();
        if (primitiveTarget == short.class)  return n.shortValue();
        if (primitiveTarget == int.class)    return n.intValue();
        if (primitiveTarget == long.class)   return n.longValue();
        if (primitiveTarget == float.class)  return n.floatValue();
        if (primitiveTarget == double.class) return n.doubleValue();
        throw new IllegalArgumentException("Unsupported primitive: " + primitiveTarget);
    }

    private static String signature(Method m) {
        StringBuilder sb = new StringBuilder(m.getName()).append("(");
        Class<?>[] p = m.getParameterTypes();
        for (int i = 0; i < p.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(p[i].getTypeName());
        }
        return sb.append(")").toString();
    }

    private static final class MethodSelection {
        final Method method;
        final Score score;
        MethodSelection(Method method, Score score) { this.method = method; this.score = score; }

        Object[] prepareArguments(Object[] provided) {
            Class<?>[] params = method.getParameterTypes();
            if (!method.isVarArgs()) {
                Object[] out = new Object[params.length];
                for (int i = 0; i < params.length; i++) {
                    out[i] = convert(provided[i], params[i]);
                }
                return out;
            } else {
                int fixed = params.length - 1;
                Object[] out = new Object[params.length];
                for (int i = 0; i < fixed; i++) {
                    out[i] = convert(provided[i], params[i]);
                }
                Class<?> varArrayType = params[params.length - 1];
                Class<?> compType = varArrayType.getComponentType();

                // If caller passed an array for the last argument position with exact arity, accept it
                if (provided.length == params.length && provided[provided.length - 1] != null
                        && varArrayType.isInstance(provided[provided.length - 1])) {
                    out[out.length - 1] = provided[provided.length - 1];
                } else {
                    int varCount = java.lang.Math.max(0, provided.length - fixed);
                    Object varArray = Array.newInstance(compType, varCount);
                    for (int i = 0; i < varCount; i++) {
                        Object converted = convert(provided[fixed + i], compType);
                        Array.set(varArray, i, converted);
                    }
                    out[out.length - 1] = varArray;
                }
                return out;
            }
        }
    }
}

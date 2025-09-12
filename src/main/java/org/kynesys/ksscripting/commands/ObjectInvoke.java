package org.kynesys.ksscripting.commands;

import org.kynesys.lwks.KSExecutionSession;
import org.kynesys.lwks.KSScriptingExecutable;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ObjectInvoke implements KSScriptingExecutable {

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
        //   ObjectInvoke {Object} <method name> <args...>
        if (args == null || args.length < 2) {
            throw new RuntimeException("ObjectInvoke requires at least 2 arguments: {Object} <method name> <args...>");
        }

        Object target = args[0];
        if (target == null) {
            throw new RuntimeException("Target object is null.");
        }
        String methodName = (String) args[1];

        Object[] methodArgs = new Object[args.length - 2];
        System.arraycopy(args, 2, methodArgs, 0, args.length - 2);

        Method method = findCompatibleMethod(target.getClass(), methodName, methodArgs);
        if (method == null) {
            throw new RuntimeException("No compatible method found for " + methodName + " in " + target.getClass().getName());
        }

        try {
            return method.invoke(target, methodArgs); // reflection will unbox when needed
        } catch (IllegalAccessException | java.lang.reflect.InvocationTargetException e) {
            throw new RuntimeException("Failed to invoke method " + methodName + " on " + target.getClass().getName(), e);
        }
    }

    private static Method findCompatibleMethod(Class<?> clazz, String name, Object[] args) {
        Method[] methods = clazz.getMethods();
        outer:
        for (Method m : methods) {
            if (!m.getName().equals(name)) continue;
            Class<?>[] params = m.getParameterTypes();
            if (params.length != args.length) continue;

            for (int i = 0; i < params.length; i++) {
                Class<?> argType = (args[i] == null) ? null : args[i].getClass();
                if (!isParamCompatible(params[i], argType)) {
                    continue outer;
                }
            }
            return m;
        }
        return null;
    }

    // Minimal compatibility: exact assignable OR primitive↔wrapper pair. No widening, no varargs.
    private static boolean isParamCompatible(Class<?> paramType, Class<?> argType) {
        if (argType == null) {
            return !paramType.isPrimitive(); // cannot assign null to a primitive
        }
        if (paramType.isAssignableFrom(argType)) return true;

        if (paramType.isPrimitive()) {
            Class<?> wrapper = PRIMITIVE_TO_WRAPPER.get(paramType);
            return wrapper != null && wrapper.equals(argType);
        } else {
            Class<?> primitive = WRAPPER_TO_PRIMITIVE.get(argType);
            return primitive != null && paramType.isAssignableFrom(PRIMITIVE_TO_WRAPPER.get(primitive));
        }
    }
}

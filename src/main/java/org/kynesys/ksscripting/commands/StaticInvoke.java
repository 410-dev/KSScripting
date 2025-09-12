package org.kynesys.ksscripting.commands;

import org.kynesys.lwks.KSExecutionSession;
import org.kynesys.lwks.KSScriptingExecutable;

public class StaticInvoke implements KSScriptingExecutable {
    @Override
    public String returnType() {
        return Object.class.getName();
    }

    @Override
    public Object execute(Object[] args, KSExecutionSession session) throws Exception {
        // Usage:
        //   StaticInvoke {Class} <method name> <args...>

        // Check if the first argument is a class
        if (args == null || args.length < 2) {
            throw new RuntimeException("StaticInvoke requires at least 2 arguments: {Class} <method name> <args...>");
        }

        Class<?> clazz = (Class<?>) args[0];
        String methodName = (String) args[1];
        Object[] methodArgs = new Object[args.length - 2];

        System.arraycopy(args, 2, methodArgs, 0, args.length - 2);
        Class<?>[] argTypes = new Class<?>[methodArgs.length];
        for (int i = 0; i < methodArgs.length; i++) {
            argTypes[i] = methodArgs[i].getClass();
        }
        try {
            // Use reflection to invoke the method
            java.lang.reflect.Method method = clazz.getMethod(methodName, argTypes);
            return method.invoke(null, methodArgs);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Method " + methodName + " not found in class " + clazz.getName(), e);
        } catch (IllegalAccessException | java.lang.reflect.InvocationTargetException e) {
            throw new RuntimeException("Failed to invoke method " + methodName + " on class " + clazz.getName(), e);
        }
    }
}

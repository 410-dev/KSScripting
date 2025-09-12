package org.kynesys.ksscripting.commands;

import org.kynesys.lwks.KSExecutionSession;
import org.kynesys.lwks.KSScriptingExecutable;

public class ObjectInvoke implements KSScriptingExecutable {
    @Override
    public String returnType() {
        return Object.class.getName();
    }

    @Override
    public Object execute(Object[] args, KSExecutionSession session) throws Exception {
        // Usage:
        //   ObjectInvoke {Object} <method name> <args...>

        // Check if the first argument is an object
        if (args == null || args.length < 2) {
            throw new RuntimeException("ObjectInvoke requires at least 2 arguments: {Object} <method name> <args...>");
        }

        Object object = args[0];
        String methodName = (String) args[1];
        Object[] methodArgs = new Object[args.length - 2];
        System.arraycopy(args, 2, methodArgs, 0, args.length - 2);
        Class<?> objectClass = object.getClass();
        Class<?>[] argTypes = new Class<?>[methodArgs.length];
        for (int i = 0; i < methodArgs.length; i++) {
            argTypes[i] = methodArgs[i].getClass();
        }
        try {
            // Use reflection to invoke the method
            java.lang.reflect.Method method = objectClass.getMethod(methodName, argTypes);
            return method.invoke(object, methodArgs);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Method " + methodName + " not found in class " + objectClass.getName(), e);
        } catch (IllegalAccessException | java.lang.reflect.InvocationTargetException e) {
            throw new RuntimeException("Failed to invoke method " + methodName + " on object of class " + objectClass.getName(), e);
        }
    }
}

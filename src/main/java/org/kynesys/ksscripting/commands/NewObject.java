package org.kynesys.ksscripting.commands;

import org.kynesys.lwks.KSExecutionSession;
import org.kynesys.lwks.KSScriptingExecutable;

public class NewObject implements KSScriptingExecutable {
    @Override
    public String returnType() {
        return Object.class.getName();
    }

    @Override
    public Object execute(Object[] args, KSExecutionSession session) throws Exception {
        // Usage:
        //   NewObject <class> <args...>
        // ex. NewObject java.lang.String "Hello, World!"

        if (args.length < 1) {
            throw new IllegalArgumentException("Class name is required");
        }
        String className = (String) args[0];
        Class<?> clazz;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Class not found: " + className, e);
        }
        Class<?>[] argTypes = new Class[args.length - 1];
        Object[] argValues = new Object[args.length - 1];
        for (int i = 1; i < args.length; i++) {
            argTypes[i - 1] = args[i].getClass();
            argValues[i - 1] = args[i];
        }

        try {
            java.lang.reflect.Constructor<?> constructor = clazz.getConstructor(argTypes);
            return constructor.newInstance(argValues);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Constructor not found for class: " + className, e);
        } catch (java.lang.reflect.InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Failed to create instance of class: " + className, e);
        }
    }
}

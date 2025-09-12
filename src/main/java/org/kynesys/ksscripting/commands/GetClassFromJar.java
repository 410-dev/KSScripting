package org.kynesys.ksscripting.commands;

import org.kynesys.lwks.KSExecutionSession;
import org.kynesys.lwks.KSScriptingExecutable;

public class GetClassFromJar implements KSScriptingExecutable {
    @Override
    public String returnType() {
        return Class.class.getName();
    }

    @Override
    public Object execute(Object[] args, KSExecutionSession session) throws Exception {
        // Usage:
        //   GetClassFromJar {jar path} <class name>
        // Check if the first argument is a jar path

        // TODO This is autoimplemented by Copilot. Make sure to check this
        System.err.println("WARNING: GetClassFromJar is not implemented yet. This is a placeholder implementation.");

        if (args == null || args.length < 2) {
            throw new RuntimeException("GetClassFromJar requires at least 2 arguments: {jar path} <class name>");
        }

        String jarPath = args[0].toString();
        String className = (String) args[1];
        String classPath = className.replace('.', '/');
        classPath = classPath + ".class";
        java.util.jar.JarFile jarFile = new java.util.jar.JarFile(jarPath);
        java.util.jar.JarEntry jarEntry = jarFile.getJarEntry(classPath);
        if (jarEntry == null) {
            throw new RuntimeException("Class " + className + " not found in jar " + jarPath);
        }
        java.io.InputStream inputStream = jarFile.getInputStream(jarEntry);
        byte[] classBytes = inputStream.readAllBytes();
        inputStream.close();
        java.lang.ClassLoader classLoader = new java.net.URLClassLoader(new java.net.URL[]{new java.io.File(jarPath).toURI().toURL()});
        java.lang.Class<?> clazz = classLoader.loadClass(className);
        return clazz;
    }
}

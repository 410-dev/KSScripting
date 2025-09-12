package org.kynesys.lwks;

public interface KSScriptingExecutable {
    String returnType();

    Object execute(Object[] args, KSExecutionSession session) throws Exception;

    default boolean isPreprocessingInterpreterWhitelistEnabled() {
        return false;
    }

    default int[] getPreprocessingInterpreterWhitelist() {
        return new int[0];
    }

    default String getManual() {return "";}
}
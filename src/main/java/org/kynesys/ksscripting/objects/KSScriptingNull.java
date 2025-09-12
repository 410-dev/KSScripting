package org.kynesys.ksscripting.objects;

public class KSScriptingNull {

    public static boolean isNull(Object o) {
        if (o == null) {
            return true;
        }
        return o instanceof KSScriptingNull;
    }

    public KSScriptingNull() {

    }

    public String toString() {
        return "KSScriptingNull";
    }
}

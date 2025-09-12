package org.kynesys.ksscripting.commands;

import org.kynesys.lwks.KSExecutionSession;
import org.kynesys.lwks.KSScriptingExecutable;

import java.util.HashMap;
import java.util.Map;

public class HostInfo implements KSScriptingExecutable {
    @Override
    public String returnType() {
        return Map.class.getName();
    }

    @Override
    public Object execute(Object[] args, KSExecutionSession session) throws Exception {
        Map<String, String> hostInfo = new HashMap<>();

        // Collect host information

        // OS
        hostInfo.put("OSName", System.getProperty("os.name"));
        hostInfo.put("OSVersion", System.getProperty("os.version"));
        hostInfo.put("OSArch", System.getProperty("os.arch"));

        // Java
        hostInfo.put("JavaVersion", System.getProperty("java.version"));
        hostInfo.put("JavaVendor", System.getProperty("java.vendor"));
        hostInfo.put("JavaVendorURL", System.getProperty("java.vendor.url"));
        hostInfo.put("JavaHome", System.getProperty("java.home"));
        hostInfo.put("JavaClassPath", System.getProperty("java.class.path"));
        hostInfo.put("JavaVMName", System.getProperty("java.vm.name"));
        hostInfo.put("JavaVMVersion", System.getProperty("java.vm.version"));

        // User
        hostInfo.put("UserName", System.getProperty("user.name"));
        hostInfo.put("UserHome", System.getProperty("user.home"));
        hostInfo.put("UserDir", System.getProperty("user.dir"));
        hostInfo.put("UserLanguage", System.getProperty("user.language"));
        hostInfo.put("UserCountry", System.getProperty("user.country"));
        hostInfo.put("UserTimezone", System.getProperty("user.timezone"));

        // System
        hostInfo.put("AvailableProcessors", String.valueOf(Runtime.getRuntime().availableProcessors()));
        hostInfo.put("FreeMemory", String.valueOf(Runtime.getRuntime().freeMemory()));
        hostInfo.put("TotalMemory", String.valueOf(Runtime.getRuntime().totalMemory()));
        hostInfo.put("MaxMemory", String.valueOf(Runtime.getRuntime().maxMemory()));

        return hostInfo;
    }
}

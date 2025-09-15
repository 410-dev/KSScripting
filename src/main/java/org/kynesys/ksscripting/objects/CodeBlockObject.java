package org.kynesys.ksscripting.objects;

import lombok.Getter;

import lombok.Setter;
import org.kynesys.ksscripting.KSScriptingInterpreter;
import org.kynesys.lwks.KSExecutionSession;

import java.util.ArrayList;

@Getter
public class CodeBlockObject implements Runnable {

    private final String name;
    @Setter private KSExecutionSession session;
    private final ArrayList<String> line;
    private int executionCount = 0;

    public CodeBlockObject(String name) {
        this.name = name;
        this.line = new ArrayList<>();
    }

    public void addCodeLine(String code) {
//        System.out.println("Adding code line: " + code);
        line.add(code);
    }

    public Object run(KSExecutionSession session) {
        executionCount += 1;
//        System.out.println("Running code block: " + name);
        String maskCodeblock = session.getEnvironment().getEnvVar().getOrDefault("MaskCodeblock", "");
        String[] lines = line.toArray(new String[0]);
        try {
            boolean maskCurrentCodeblock = maskCodeblock.contains(name + ";") || maskCodeblock.equals("1");
            if (maskCurrentCodeblock) {
                session.getEnvironment().getEnvVar().remove("MaskCodeblock");
            } else {
                session.getEnvironment().getEnvVar().put("CurrentCodeblockSession", name);
            }
            Object returned = KSScriptingInterpreter.executeLines(lines, session);
            if (maskCurrentCodeblock) {
                session.getEnvironment().getEnvVar().remove("CurrentCodeblockSession");
            } else {
                session.getEnvironment().getEnvVar().put("MaskCodeblock", maskCodeblock);
            }
            return returned;
        } catch (Exception e) {
            throw new RuntimeException("Error executing code block: " + name, e);
        }
    }

    @Override
    public void run() {
        run(session);
    }

    @Override
    public String toString() {
        return "Codeblock: " + name + " (" + line.size() + " lines, " + executionCount + " executions)";
    }
}

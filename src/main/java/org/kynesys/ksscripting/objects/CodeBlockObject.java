package org.kynesys.ksscripting.objects;

import lombok.Getter;

import org.kynesys.ksscripting.KSScriptingInterpreter;
import org.kynesys.lwks.KSExecutionSession;

import java.util.ArrayList;

@Getter
public class CodeBlockObject {

    private final String name;
    private final ArrayList<String> line;

    public CodeBlockObject(String name) {
        this.name = name;
        this.line = new ArrayList<>();
    }

    public void addCodeLine(String code) {
//        System.out.println("Adding code line: " + code);
        line.add(code);
    }

    public Object run(KSExecutionSession session) {
//        System.out.println("Running code block: " + name);
        String[] lines = line.toArray(new String[0]);
        try {
            return KSScriptingInterpreter.executeLines(lines, session);
        } catch (Exception e) {
            throw new RuntimeException("Error executing code block: " + name, e);
        }
    }
}

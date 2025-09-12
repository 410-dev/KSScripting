package org.kynesys.ksscripting.commands;

import lombok.Getter;
import org.kynesys.ksscripting.objects.CodeBlockObject;
import org.kynesys.lwks.KSExecutionSession;
import org.kynesys.lwks.KSScriptingExecutable;

@Getter
public class Codeblock implements KSScriptingExecutable {

    private final boolean preprocessingInterpreterWhitelistEnabled = true;
    private final int[] preprocessingInterpreterWhitelist = new int[]{0, 1}; // 0: name, 1: action

    @Override
    public String returnType() {
        return CodeBlockObject.class.getName();
    }

    @Override
    public Object execute(Object[] args, KSExecutionSession session) throws Exception {
        // Usage:
        //   Codeblock <name> <action> <?code?>
        //   actions: "make", "add", "run"
        if (args == null || args.length < 2) {
            throw new RuntimeException("Codeblock requires at least 2 arguments: <name> <action|make, add, run> <code:optional>");
        }

        String name = (String) args[0];
        String action = (String) args[1];
        CodeBlockObject codeBlock = (CodeBlockObject) session.getComplexVariable(CodeBlockObject.class.getCanonicalName() + ":__BLOCKINSTANCE__:" + name);
        if (codeBlock == null) {
            codeBlock = new CodeBlockObject(name);
            session.setComplexVariable(CodeBlockObject.class.getCanonicalName() + ":__BLOCKINSTANCE__:" + name, codeBlock);
        }
        switch (action) {
            case "make" -> {
                // Create a new code block
                codeBlock = new CodeBlockObject(name);
                session.setComplexVariable(CodeBlockObject.class.getCanonicalName() + ":__BLOCKINSTANCE__:" + name, codeBlock);
                return codeBlock;
            }
            case "add" -> {
                // Add code to the code block
                if (args.length < 3) {
                    throw new RuntimeException("Codeblock add requires at least 3 arguments: <name> add <code>");
                }
                StringBuilder code = new StringBuilder();
                for (int i = 2; i < args.length; i++) {
                    Object arg = args[i];
                    if (arg instanceof String) {
                        code.append(arg);
                    } else if (arg instanceof Number) {
                        code.append(arg);
                    } else {
                        code.append(arg.getClass().getName()).append(": ").append(arg);
                    }
                    if (i < args.length - 1) {
                        code.append(" ");
                    }
                }
                codeBlock.addCodeLine(code.toString());
                return codeBlock;
            }
            case "run" -> {
                // Run the code block
                return codeBlock.run(session);
            }
            default -> throw new RuntimeException("Codeblock action must be one of: make, add, run");
        }
    }
}

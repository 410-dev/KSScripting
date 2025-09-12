package org.kynesys.ksscripting.commands;

import lombok.Getter;
import org.kynesys.lwks.KSExecutionSession;
import org.kynesys.lwks.KSScriptingExecutable;
import org.kynesys.ksscripting.KSScriptingInterpreter;

@Getter
public class Asynchronize implements KSScriptingExecutable {

    private final boolean preprocessingInterpreterWhitelistEnabled = true;
    private final int[] preprocessingInterpreterWhitelist = new int[]{};

    @Override
    public String returnType() {
        return Thread.class.getName();
    }

    @Override
    public Object execute(Object[] args, KSExecutionSession session) throws Exception {
        // Usage:
        //   Asynchronize <code>
        if (args == null || args.length < 1) {
            throw new RuntimeException("Asynchronize requires at least one command");
        }

        Thread running = new Thread(() -> {
            KSScriptingInterpreter.execute(args, session);
        });
        running.start();
        return running;
    }
}

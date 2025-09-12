package org.kynesys.ksscripting.graphickit;

import lombok.Getter;
import org.kynesys.lwks.KSExecutionSession;
import org.kynesys.lwks.KSScriptingExecutable;
import org.kynesys.ksscripting.KSScriptingInterpreter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
public class WaitUntilDetected implements KSScriptingExecutable {

    private final boolean preprocessingInterpreterWhitelistEnabled = true;
    private final int[] preprocessingInterpreterWhitelist = new int[]{0, 1}; // 0: latency, 1: minLength

    @Override
    public String returnType() {
        return "";
    }

    @Override
    public Object execute(Object[] args, KSExecutionSession session) throws Exception {
        // Usage:
        //    WaitUntilDetected <latency in MS> <minimum length> <command that returns any iterable...>

        if (args == null || args.length < 3) {
            throw new RuntimeException("WaitUntilDetected requires at least 3 arguments: <latency> <minLength> <command> but got: " + (args == null ? 0 : args.length));
        }
        Object latencyObj = args[0];
        Object minLengthObj = args[1];
        if (!(latencyObj instanceof Number) || !(minLengthObj instanceof Number)) {
            throw new RuntimeException("WaitUntilDetected requires the first two arguments to be numbers");
        }

        double latency = ((Number) latencyObj).doubleValue();
        double minLength = ((Number) minLengthObj).doubleValue();

        if (latency < 0 || minLength < 0) {
            throw new RuntimeException("WaitUntilDetected requires the first two arguments to be non-negative numbers");
        }

        // Build string
        Object[] commandParts = new Object[args.length - 2];

        Collection<?> resultCol = new ArrayList<>();
        List<?> resultList = new ArrayList<>();
        Object[] resultArray = new Object[0];

        while (resultList.size() < minLength && resultCol.size() < minLength && resultArray.length < minLength) {
            System.arraycopy(args, 2, commandParts, 0, args.length - 2);
            Object result = KSScriptingInterpreter.execute(commandParts, session);
            switch (result) {
                case List<?> objects -> resultList = objects;
                case Object[] objects -> resultArray = objects;
                case Collection<?> objects -> resultCol = objects;
                case null, default ->
                        throw new RuntimeException("WaitUntilDetected requires the command to return a List, Object[], or Collection");
            }
            if (resultList.size() >= minLength || resultCol.size() >= minLength || resultArray.length >= minLength) {
                break;
            }
            try {
                Thread.sleep((long) (latency));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("WaitUntilDetected interrupted", e);
            }
        }

        if (resultList.size() >= minLength) {
            return resultList;
        } else if (resultCol.size() >= minLength) {
            return resultCol;
        } else {
            return resultArray;
        }
    }
}

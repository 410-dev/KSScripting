package org.kynesys.lwks;

import java.util.ArrayList;
import java.util.List;

public class KSStringController {
    public static String[] splitStringAsArguments(String str) {
        // Split the string into arguments based on spaces, quotes, and consider escaping quotes as well
        // Ex: "Hello world" "This is a \"test\"" and this is also a test {and this is also a test} {{and this is also a test}}
        // This should split as ["Hello world", "This is a \"test\"", "and", "this", "is", "also", "a", "test", "{and this is also a test}", "{{and this is also a test}}"]

        StringBuilder currentArg = new StringBuilder();
        boolean inQuotes = false;
        boolean escapeNext = false;
        boolean inBraces = false;
        int braceDepthCounter = 0;
        List<String> args = new ArrayList<>();

        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);

            if (escapeNext) {
                currentArg.append(c);
                escapeNext = false;
                continue;
            }

            if (c == '\\') {
                escapeNext = true;
                continue;
            }

            if (c == '"' && !inBraces) {
                inQuotes = !inQuotes;
                continue;
            }

            if (c == '{' && !inQuotes) {
                inBraces = true;
                braceDepthCounter++;
                currentArg.append(c);
                continue;
            }

            if (c == '}' && !inQuotes) {
                braceDepthCounter--;
                if (braceDepthCounter == 0) {
                    inBraces = false;
                }
                currentArg.append(c);
                continue;
            }

            if (!inQuotes && !inBraces && Character.isWhitespace(c)) {
                if (!currentArg.isEmpty()) {
                    args.add(currentArg.toString());
                    currentArg.setLength(0);
                }
                continue;
            }

            currentArg.append(c);
        }

        if (!currentArg.isEmpty()) {
            args.add(currentArg.toString());
        }
        return args.toArray(new String[0]);
    }

    public static String[] batchSplit(String str, String[] delimiters) {
        List<String> result = new ArrayList<>();
        StringBuilder currentPart = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            boolean isDelimiter = false;
            for (String delimiter : delimiters) {
                if (str.startsWith(delimiter, i)) {
                    isDelimiter = true;
                    if (!currentPart.isEmpty()) {
                        result.add(currentPart.toString());
                        currentPart.setLength(0);
                    }
                    i += delimiter.length() - 1; // Skip the delimiter
                    break;
                }
            }
            if (!isDelimiter) {
                currentPart.append(c);
            }
        }
        return result.toArray(new String[0]);
    }
}
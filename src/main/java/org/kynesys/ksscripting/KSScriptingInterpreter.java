package org.kynesys.ksscripting;



import org.kynesys.lwks.KSEnvironment;
import org.kynesys.lwks.KSExecutionSession;
import org.kynesys.lwks.KSScriptingExecutable;
import org.kynesys.lwks.KSStringController;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.util.Scanner;

public class KSScriptingInterpreter {

    public static Object executeLine(String line, KSExecutionSession session) {
        String[] linePartsInString = KSStringController.splitStringAsArguments(line);
        Object[] lineParts = new Object[linePartsInString.length];

        // Copy linePartsInString to lineParts
        for (int i = 0; i < linePartsInString.length; i++) {
            String part = linePartsInString[i];
            if (part == null || part.isEmpty()) {
                lineParts[i] = null;
            } else {
                lineParts[i] = part;
            }
        }

        // Execute the command
        return execute(lineParts, session);
    }

    public static Object execute(Object[] lineParts, KSExecutionSession session) {

        if (session.isSessionTerminated()) {
            return session.getTerminatingValue();
        }

        StringBuilder lineBuilder = new StringBuilder();
        for (Object part : lineParts) {
            if (part != null) {
                lineBuilder.append(part).append(" ");
            }
        }

        if (lineParts.length == 0) {
            return 1;
        }

        // Check if line starts with //
        if (lineParts[0] instanceof String linePart && linePart.startsWith("//")) {
            return 0; // Ignore comment
        }

        // Check command
        Object commandLocation = lineParts[0];
        if (!(commandLocation instanceof String command)) {
            throw new RuntimeException("Command name must be a string");
        }
        if (command.isEmpty()) {
            throw new RuntimeException("Command name cannot be empty");
        }
        if (command.contains(" ")) {
            throw new RuntimeException("Command name cannot contain spaces");
        }

        // Get command class
        // If command contains ".", use it as the full class name
        // Else, try to find the class in the package paths
        Class<?> commandClass = null;
        if (command.contains(".")) {
            try {
                commandClass = Class.forName(command);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Command class not found (Auto pathing not used): " + command, e);
            }
        } else {
            for (int i = 0; i < session.getPackagePaths().size(); i++) {
                try {
                    commandClass = Class.forName(session.getPackagePaths().get(i) + "." + command);
                } catch (ClassNotFoundException e) {
                    continue;
                }
            }
        }
        if (commandClass == null) {
            throw new RuntimeException("Command class not found: " + command);
        }


        // Check if commandClass is a subclass of KSScriptingExecutable
        if (!KSScriptingExecutable.class.isAssignableFrom(commandClass)) {
            throw new RuntimeException("Command class does not implement KSScriptingExecutable");
        }

        KSScriptingExecutable commandInstance;
        try {
            commandInstance = (KSScriptingExecutable) commandClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create command instance", e);
        }

        // Check if whitelist is enabled
        int[] whitelist = commandInstance.getPreprocessingInterpreterWhitelist();
        boolean isWhitelistEnabled = commandInstance.isPreprocessingInterpreterWhitelistEnabled();

        // Check if there's any part that starts with "{"
        for (int i = 0; i < lineParts.length; i++) {

            // Skip the un-whitelisted parts
            if (isWhitelistEnabled) {
                boolean whitelisted = false;
                for (int k : whitelist) {
                    if (i - 1 == k) {
                        whitelisted = true;
                        break;
                    }
                }
                if (!whitelisted) {
                    continue;
                }
            }

            Object part = lineParts[i];

            if (part == null) {
                continue;
            }

            // Interpret only string
            if (part instanceof String partStr) {

                // Double brace: Replace with variable in session
                if (partStr.startsWith("{{") && partStr.endsWith("}}")) {
                    String variableName = partStr.substring(2, partStr.length() - 2);
                    Object variableValue = session.getComplexVariable(variableName);
                    if (variableValue != null) {
                        lineParts[i] = variableValue;
                    } else {
                        throw new RuntimeException("Variable " + variableName + " not found in session");
                    }
                }

                // Single brace: Replace with the result returned from the execution
                else if (partStr.startsWith("{") && partStr.endsWith("}")) {
                    String subCommand = partStr.substring(1, partStr.length() - 1);
                    Object result;
                    try {
                        result = executeLine(subCommand, session);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to execute command \"" + subCommand + "\" in argument " + i + ": \"" + lineBuilder + "\"", e);
                    }
                    lineParts[i] = result;
                }
            }
        }

        // Make arguments array
        Object[] commandArgs = new Object[lineParts.length - 1];
        System.arraycopy(lineParts, 1, commandArgs, 0, lineParts.length - 1);


        // Execute command
        Object result;
        try {
            result = commandInstance.execute(commandArgs, session);
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute command \"" + command + "\" at line: \"" + lineBuilder + "\"", e);
        }

        // Return result
        return result;
    }

    public static Object executeLines(String[] lines, KSExecutionSession session) {
        Object result = null;
        for (int i = 0; i < lines.length; i++) {
            if (session.isSessionTerminated()) {
                return session.getTerminatingValue();
            }
            String line = lines[i];
            System.out.println("// Executing line " + (i + 1) + ": \"" + line + "\"");
            if (line == null || line.isEmpty()) {
                continue;
            }
            try {
                result = executeLine(line, session);
                session.setLastResult(result);
//                System.out.println("// Exited: " + result + " (" + (result != null ? result.getClass().getSimpleName() : "null") + ")");
            } catch (Exception e) {
                throw new RuntimeException("Failed to execute line " + (i + 1) + ": \"" + line + "\"", e);
            }
        }
        return result;
    }

    public static Object executeLines(String[] lines) {
        KSEnvironment environment = new KSEnvironment();
        KSExecutionSession session = new KSExecutionSession(environment);
        return executeLines(lines, session);
    }

    public static void main(String[] args) {
//        String[] lines = {
//                "Print {GetAsInteger64 123}",
//                "StoreValue v1 = {GetAsInteger64 1}",
//                "StoreValue v2 = {GetAsInteger64 2}",
//                "StoreValue v3 = {GetAsInteger64 3}",
//                "StoreValue v4 = {GetAsInteger64 4}",
//                "StoreValue v4 = {And {CompareNumber {{v1}} > {{v2}}} {CompareNumber {{v3}} < {{v4}}}}",
//                "StoreValue v5 = {GetAsInteger64 10}",
//                "RunIf {{v4}} Print Hello",
//                "RunIf {Not {{v4}}} RunIf {Not {{v4}}} Print Yaaay",
//                "// This is comment",
//                "Codeblock test make",
//                "Codeblock test add Print {{i}}",
//                "For i in {Range {{v1}} {{v5}}} Codeblock test run",
//                "// Test incremental",
//                "Codeblock whileloop make",
//                "Codeblock whileloop add Print While: {{i}}",
//                "Codeblock whileloop add StoreValue i = {Math add {{i}} {GetAsInteger64 1}}",
//                "StoreValue i = {GetAsInteger64 0}",
//                "StoreValue max = {GetAsInteger64 10}",
//                "While {CompareNumber {{i}} < {{max}}} Codeblock whileloop run",
//                "Print {GetFromMap {HostInfo} OSName}",

//        };

//        System.out.println("==========================SESSION==========================");
//        executeLines(lines);
//        System.out.println("============================END============================");

//
//
//        JFrame frame = new JFrame();
//        JPanel panel = new JPanel();
//        JButton button = new JButton("Click Me");
//        button.addActionListener(e -> {
//            System.out.println("Button clicked!");
//            JOptionPane.showMessageDialog(frame, "Button clicked!", "Message", JOptionPane.INFORMATION_MESSAGE);
//        });
//
//        button.addMouseListener(new MouseAdapter() {
//            @Override
//            public void mouseClicked(java.awt.event.MouseEvent e) {
//                System.out.println("Mouse clicked!");
//            }
//        });
//
//        JTextField textField = new JTextField();
//
//        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
//        panel.add(button);
//        panel.add(textField);
//        frame.setContentPane(panel);
//        frame.setTitle("KSScriptingInterpreter");
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.setSize(300, 200);
//        frame.setVisible(true);


        // Example of using the execute method
        KSExecutionSession session = new KSExecutionSession(new KSEnvironment());
//        session.setComplexVariable("button", button);
//        session.setComplexVariable("panel", panel);
//        session.setComplexVariable("frame", frame);
//        session.setComplexVariable("textField", textField);

//        String[] script = {
//                "Asynchronize SocketOpen 9090 {Iterable} "
//        };

//        String[] script = {
//                "// Wait until the window is open",
//                "Delay 1000",
//                "",
//                "// Enumerate all open windows",
//                "StoreValue windowsOpen = {GCEnumerateOpenWindows frame}",
//                "",
//                "// Filter by title (contains KS)",
//                "StoreValue windowsFiltered = {GCFilterWindowsByPattern {{windowsOpen}} javax.swing.JFrame getTitle contains KS}",
//                "StoreValue targetWindow = {GetItemFromIterable {{windowsFiltered}} {Int 0}}",
//                "StoreValue contentPane = {ObjectInvoke {{targetWindow}} getContentPane}",
//                "",
//                "// Find the text field in the content pane",
//                "StoreValue textField = {GetItemFromIterable {GCFilterComponentsByPattern notraverse {{contentPane}} javax.swing.JTextField getText isAny _} {Int 0}}",
//                "GCControlledInput {{textField}} mouseClick",
//                "GCControlledInput {{textField}} keyboard 0 Hello World",
////                "Delay 2",
//                "",
//                "// Find the button in the content pane",
//                "StoreValue button = {GetItemFromIterable {WaitUntilDetected {Int 1} {Int 1} GCFilterComponentsByPattern notraverse {{contentPane}} javax.swing.JButton getText contains Click} {Int 0}}",
//                "GCControlledInput {{button}} mouseClick",
////                "Delay 80",
//                "",
//                "// Find open JOptionPanes",
//                "StoreValue filtered = {WaitUntilDetected {Int 1} {Int 1} GCFilterWindowsByPattern {GCEnumerateOpenWindows dialog} javax.swing.JDialog getTitle contains Message}",
//                "StoreValue targetPopup = {GetItemFromIterable {{filtered}} {Int 0}}",
//                "StoreValue popupContentPane = {ObjectInvoke {{targetPopup}} getContentPane}",
//                "StoreValue filtered = {GCFilterComponentsByPattern traverse {{popupContentPane}} javax.swing.JButton getText isAny _}",
//                "StoreValue buttonComponent = {GetItemFromIterable {{filtered}} {Int 0}}",
//                "",
//                "// Click the button in the popup",
//                "GCControlledInput {{buttonComponent}} mouseClick",
//                "",
//                "// Try opening new popup",
//                "StoreValue userOutput = {GCPopupAlert \"Title goes here\" \"This is a test popup\" info \"Fuck You\" \"Nothing\"}",
//                "Print User pressed: {{userOutput}}",
//        };

//        String[] script = {
//                "StoreValue map = {Map , hello world}",
//                "Print {{map}}",
//                "StoreValue map2 = {AddItemToMap {{map}} test void}",
//                "Print {{map}}",
//                "Print {{map2}}"
//        };
//        executeLines(script, session);

        if (args.length == 0) {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String currentDirectory = System.getProperty("user.dir");
                String currentUsername = System.getProperty("user.name");
                String currentUserHome = System.getProperty("user.home");
                String inputHeadFormat = session.getEnvironment().getEnvVar().getOrDefault("ShellInputHead", "{Username}@{CurrentDirectoryWithSimplifyIfHome} # ");
                inputHeadFormat = inputHeadFormat.replace("{Username}", currentUsername);
//                inputHeadFormat = inputHeadFormat.replace("{MachineName}", currentUserHome);
                inputHeadFormat = inputHeadFormat.replace("{CurrentDirectoryWithSimplifyIfHome}", currentDirectory.equals(currentUserHome) ? "~" : currentDirectory);
                inputHeadFormat = inputHeadFormat.replace("{CurrentDirectory}", currentDirectory);
                System.out.print(inputHeadFormat);
                try {
                    Object output = executeLine(scanner.nextLine(), session);
                    session.setLastResult(output);
                    if (session.getEnvironment().getEnvVar().getOrDefault("PrintResult", "").equals("1")) {
                        System.out.println(output);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            execute(args, session);
        }
    }
}

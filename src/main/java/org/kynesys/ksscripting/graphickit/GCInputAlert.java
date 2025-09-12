package org.kynesys.ksscripting.graphickit;

import org.kynesys.ksscripting.objects.KSScriptingNull;
import org.kynesys.lwks.KSExecutionSession;
import org.kynesys.lwks.KSScriptingExecutable;

import javax.swing.*;

public class GCInputAlert implements KSScriptingExecutable {
    @Override
    public String returnType() {
        return JOptionPane.class.getName();
    }

    @Override
    public Object execute(Object[] args, KSExecutionSession session) throws Exception {
        // Usage:
        //   GCInputAlert title message <result store variable name> <hide input: bool true | false> <buttons>
        // ex. GCInputAlert "Title" "Message" result {Bool true} "OK" "Cancel" "Ignore" ...

        if (args == null || args.length < 5) {
            throw new RuntimeException("GCInputAlert requires at least 5 arguments: title message result <hide input: bool true | false> <buttons: buttons strings> but got " + (args == null ? 0 : args.length));
        }

        String title = (String) args[0];
        String message = (String) args[1];
        String storeTo = (String) args[2];
        boolean hideFieldFlag = false;
        if (args[3] instanceof KSScriptingNull) {
            hideFieldFlag = false;
        } else if (args[3] instanceof Boolean) {
            hideFieldFlag = (Boolean) args[3];
        } else {
            throw new RuntimeException("GCInputAlert hide input flag must be a boolean");
        }
        String[] buttons = new String[args.length - 4];
        for (int i = 4; i < args.length; i++) {
            buttons[i - 4] = (String) args[i];
        }

        // Create a panel to hold the input field
        JPanel panel = new JPanel();
        JPasswordField passwordFieldInput = null;
        JTextField inputField = null;
        panel.add(new JLabel(message));
        if (hideFieldFlag) {
            passwordFieldInput = new JPasswordField(20);
            panel.add(new JLabel(message));
            panel.add(passwordFieldInput);
        } else {
            // Create a text field for input
            inputField = new JTextField(20);
            panel.add(inputField);
        }

        // Show the input dialog
        int result = JOptionPane.showOptionDialog(null, panel, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, buttons, buttons[0]);
        String input;
        if (hideFieldFlag) {
            input = new String(passwordFieldInput.getPassword());
        } else {
            input = inputField.getText();
        }
        // Handle the result
        session.setComplexVariable(storeTo, input);
        return buttons[result];
    }
}

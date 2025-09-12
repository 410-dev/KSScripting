package org.kynesys.ksscripting.graphickit;

import org.kynesys.lwks.KSExecutionSession;
import org.kynesys.lwks.KSScriptingExecutable;

import javax.swing.*;

public class GCPopupAlert implements KSScriptingExecutable {
    @Override
    public String returnType() {
        return JOptionPane.class.getName();
    }

    @Override
    public Object execute(Object[] args, KSExecutionSession session) throws Exception {
        // Usage:
        //   GCPopupAlert title message <store variable name> <type: info | warning | error | question> <buttons: buttons strings>
        // ex. GCPopupAlert "Title" "Message" result "info" "OK" "Cancel" "Ignore" ...

        if (args == null || args.length < 4) {
            throw new RuntimeException("GCPopupAlert requires at least 4 arguments: title message <type: info | warning | error | question> <buttons: buttons strings> but got " + (args == null ? 0 : args.length));
        }

        String title = (String) args[0];
        String message = (String) args[1];
        String type = (String) args[2];
        String[] buttons = new String[args.length - 3];
        for (int i = 3; i < args.length; i++) {
            buttons[i - 3] = (String) args[i];
        }

        int messageType;
        switch (type.toLowerCase()) {
            case "info" -> messageType = JOptionPane.INFORMATION_MESSAGE;
            case "warning" -> messageType = JOptionPane.WARNING_MESSAGE;
            case "error" -> messageType = JOptionPane.ERROR_MESSAGE;
            case "question" -> messageType = JOptionPane.QUESTION_MESSAGE;
            default -> throw new RuntimeException("GCPopupAlert type must be one of: info, warning, error, question");
        }

        int result = JOptionPane.showOptionDialog(null, message, title, JOptionPane.DEFAULT_OPTION, messageType, null, buttons, buttons[0]);
        return result == -1 ? "CLOSED" : buttons[result];
    }
}

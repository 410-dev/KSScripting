package org.kynesys.ksscripting.graphickit;

import org.kynesys.lwks.KSExecutionSession;
import org.kynesys.lwks.KSScriptingExecutable;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GCEnumerateOpenWindows implements KSScriptingExecutable {

    @Override
    public String returnType() {
        return List.class.getName();
    }

    @Override
    public Object execute(Object[] args, KSExecutionSession session) throws Exception {
        // Usage:
        //   GCEnumerateOpenWindows <frame | dialog> <parent (or desktop, null if not needed)>

        // Check if the first argument is a string
        if (args == null || args.length < 1) {
            throw new RuntimeException("GCEnumerateOpenWindows requires at least 1 argument: <frame | dialog> [<parent>]");
        }

        String type = (String) args[0];
        Object parent = args.length > 1 ? args[1] : null;
        if (parent != null && !(parent instanceof Container)) {
            throw new RuntimeException("Second argument must be a Container or null.");
        }

        if (type.equals("frame")) {
            return getOpenJFrames();
        } else if (type.equals("dialog")) {
            if (parent instanceof JDesktopPane desktop) {
                return getOpenJInternalFrames(desktop);
            } else {
                return getOpenOptionPanes();
            }
        } else {
            throw new RuntimeException("GCEnumerateOpenWindows requires a type of 'frame' or 'dialog'");
        }
    }

    public static List<JFrame> getOpenJFrames() {
        return Arrays.stream(Frame.getFrames())
                .filter(f -> f.isShowing() && f instanceof JFrame)
                .map(f -> (JFrame)f)
                .collect(Collectors.toList());
    }

    public static List<JInternalFrame> getOpenJInternalFrames(JDesktopPane desktop) {
        return Arrays.stream(desktop.getAllFrames())
                .filter(JInternalFrame::isVisible)
                .collect(Collectors.toList());
    }

    public static List<JDialog> getOpenOptionPanes() {
        return Arrays.stream(Window.getWindows())
                .filter(w -> w instanceof JDialog && w.isShowing())
                .map(w -> (JDialog)w)
                .filter(d -> {
                    Component c = d.getContentPane().getComponent(0);
                    return c instanceof JOptionPane;
                })
                .collect(Collectors.toList());
    }


}

package org.kynesys.ksscripting.graphickit;

import org.kynesys.ksscripting.objects.CodeBlockObject;
import org.kynesys.ksscripting.objects.KSScriptingNull;
import org.kynesys.lwks.KSExecutionSession;
import org.kynesys.lwks.KSScriptingExecutable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Locale;
import java.util.Objects;

public class EventBind implements KSScriptingExecutable {

    public EventBind() {}

    // --------------------------
    // Public DSL-facing methods
    // --------------------------

    /** Maps: "press"/"down" -> keyPressed, "release"/"up" -> keyReleased, "type"/"typed" -> keyTyped. */
    public static void addKeyEvent(Component c, String eventType, Runnable action) {
        Objects.requireNonNull(c, "component");
        Runnable safe = wrapOnEDT(action);
        switch (norm(eventType)) {
            case "press", "down", "keypressed" -> c.addKeyListener(new KeyAdapter() {
                @Override public void keyPressed(KeyEvent e) { safe.run(); }
            });
            case "release", "up", "keyreleased" -> c.addKeyListener(new KeyAdapter() {
                @Override public void keyReleased(KeyEvent e) { safe.run(); }
            });
            case "type", "typed", "keytyped" -> c.addKeyListener(new KeyAdapter() {
                @Override public void keyTyped(KeyEvent e) { safe.run(); }
            });
            default -> throw unsupported("key", eventType,
                    "press/down", "release/up", "type/typed");
        }

        // Practical: components often ignore key events unless focusable
        ensureFocusableForKeys(c);
    }

    /**
     * Mouse mappings:
     *  - "click" -> mouseClicked
     *  - "dblclick"/"doubleclick" -> mouseClicked with clickCount==2
     *  - "press"/"down" -> mousePressed
     *  - "release"/"up" -> mouseReleased
     *  - "enter"/"over" -> mouseEntered
     *  - "exit"/"out" -> mouseExited
     *  - "move" -> mouseMoved
     *  - "drag" -> mouseDragged
     *  - "wheel"/"scroll" -> mouseWheelMoved
     */
    public static void addMouseEvent(Component c, String eventType, Runnable action) {
        Objects.requireNonNull(c, "component");
        Runnable safe = wrapOnEDT(action);
        switch (norm(eventType)) {
            case "click", "clicked" -> c.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) { safe.run(); }
            });
            case "dblclick", "doubleclick", "double-click" -> c.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) safe.run();
                }
            });
            case "press", "down", "mousepressed" -> c.addMouseListener(new MouseAdapter() {
                @Override public void mousePressed(MouseEvent e) { safe.run(); }
            });
            case "release", "up", "mousereleased" -> c.addMouseListener(new MouseAdapter() {
                @Override public void mouseReleased(MouseEvent e) { safe.run(); }
            });
            case "enter", "over", "mouseenter" -> c.addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { safe.run(); }
            });
            case "exit", "out", "mouseexit", "mouseleave" -> c.addMouseListener(new MouseAdapter() {
                @Override public void mouseExited(MouseEvent e) { safe.run(); }
            });
            case "move", "mousemove" -> c.addMouseMotionListener(new MouseMotionAdapter() {
                @Override public void mouseMoved(MouseEvent e) { safe.run(); }
            });
            case "drag", "mousedrag" -> c.addMouseMotionListener(new MouseMotionAdapter() {
                @Override public void mouseDragged(MouseEvent e) { safe.run(); }
            });
            case "wheel", "scroll", "mousewheel" -> c.addMouseWheelListener(e -> safe.run());
            default -> throw unsupported("mouse", eventType,
                    "click", "dblclick", "press/down", "release/up", "enter/over",
                    "exit/out", "move", "drag", "wheel/scroll");
        }
    }

    // --------------------------
    // Internals
    // --------------------------

    private static String norm(String s) {
        if (s == null) return "";
        return s.trim().toLowerCase(Locale.ROOT);
    }

    private static IllegalArgumentException unsupported(String kind, String got, String... allowed) {
        return new IllegalArgumentException(
                "Unsupported " + kind + " event type: \"" + got + "\". Allowed: " + String.join(", ", allowed));
    }

    /**
     * Swing delivers events on the EDT. Ensure your Runnable runs on the EDT too,
     * but without blocking if the event already arrived there.
     */
    private static Runnable wrapOnEDT(Runnable r) {
        return () -> {
            if (SwingUtilities.isEventDispatchThread()) {
                r.run();
            } else {
                SwingUtilities.invokeLater(r);
            }
        };
    }

    /**
     * Most Swing components ignore key events unless focusable and focused.
     * This is a sane one-liner fix that you should always do for key hooks.
     */
    private static void ensureFocusableForKeys(Component c) {
        if (c instanceof JComponent jc) {
            if (!jc.isFocusable()) jc.setFocusable(true);
        } else {
            c.setFocusable(true);
        }
        // Best effort: if already displayable, try to grab focus.
        if (c.isDisplayable()) c.requestFocusInWindow();
    }

    @Override
    public String returnType() {
        return KSScriptingNull.class.getName();
    }

    @Override
    public Object execute(Object[] args, KSExecutionSession session) throws Exception {
        // Usage:
        //   EventBind {{object}} "mouse/keyboard" "event" {{codeblock}}
        //
        // Events:
        //    `"down"` → `keyPressed`
        //    `"up"` → `keyReleased`
        //    `"type"` → `keyTyped`
        //
        //* **Mouse events**:
        //
        //    `"click"` → `mouseClicked`
        //    `"dblclick"` → `mouseClicked` with `getClickCount()==2`
        //    `"down"` → `mousePressed`
        //    `"up"` → `mouseReleased`
        //    `"enter"` → `mouseEntered`
        //    `"exit"` → `mouseExited`
        //    `"move"` → `mouseMoved`
        //    `"drag"` → `mouseDragged`
        //    `"wheel"` → `mouseWheelMoved`
        //

        if (args.length != 4) {
            throw new IllegalArgumentException("Required 4 ({{object}}, \"mouse/keyboard\", \"event-id\", {{codeblock}}) parameter but given either more or less parameters.");
        }

        if (!(args[0] instanceof Component)) {
            throw new IllegalArgumentException("First parameter must be instance of AWT Component, but is not.");
        }

        if (!(args[1] instanceof String)) {
            throw new IllegalArgumentException("Second parameter must be instance of String, but is not.");
        }

        if (!args[1].equals("mouse") && !args[1].equals("keyboard")) {
            throw new IllegalArgumentException("Second parameter must be either 'mouse' or 'keyboard'.");
        }

        if (!(args[2] instanceof String)) {
            throw new IllegalArgumentException("Third parameter must be instance of String, but is not.");
        }

        if (!(args[3] instanceof CodeBlockObject)) {
            throw new IllegalArgumentException("Fourth parameter must be instance of CodeBlockObject, but is not.");
        }

        if (args[1].equals("mouse")) {
            String[] avbl = new String[]{"click", "double-click", "down", "up", "enter", "exit", "move", "drag", "wheel"};
            chkAction(args, avbl);
            EventBind.addMouseEvent((Component) args[0], args[2].toString(), (Runnable) args[3]);
        } else {
            String[] avbl = new String[]{"down", "up", "type"};
            chkAction(args, avbl);
            EventBind.addKeyEvent((Component) args[0], args[2].toString(), (Runnable) args[3]);
        }
        return new KSScriptingNull();
    }

    private void chkAction(Object[] args, String[] avbl) {
        boolean found = false;
        for (String s : avbl) {
            if (s.equals(args[2])) {
                found = true;
                break;
            }
        }
        if (!found) {
            throw new  IllegalArgumentException("Third parameter must be any of: " +  String.join(", ", avbl));
        }
    }
}

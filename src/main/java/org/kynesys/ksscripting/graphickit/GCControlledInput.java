package org.kynesys.ksscripting.graphickit;

import org.kynesys.ksscripting.objects.KSScriptingNull;
import org.kynesys.lwks.KSExecutionSession;
import org.kynesys.lwks.KSScriptingExecutable;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Objects;

public class GCControlledInput implements KSScriptingExecutable {
    @Override
    public String returnType() {
        return KSScriptingNull.class.getName();
    }

    @Override
    public Object execute(Object[] args, KSExecutionSession session) throws Exception {
        // Usage: GCControlledInput <component object> <keyboard | mouseClick> <event constructor args...>
        if (args == null || args.length < 2) {
            throw new RuntimeException("GCControlledInput requires at least 2 arguments: <component> <type: keyboard | mouseClick> <args?>, but got " + Objects.requireNonNull(args).length + ".");
        }
        Object component = args[0];
        String type = (String) args[1];
        Object[] eventArgs = new Object[args.length - 2];
        System.arraycopy(args, 2, eventArgs, 0, args.length - 2);

        if (!(component instanceof Component c)) {
            throw new RuntimeException("GCControlledInput requires a component as the first argument");
        }

        if (type.equals("keyboard")) {
            keyboard(c, eventArgs);
        } else if (type.equals("mouseClick")) {
            mouseClick(c, eventArgs);
        } else {
            throw new RuntimeException("GCControlledInput requires a type of 'keyboard' or 'mouseClick'");
        }

        return new KSScriptingNull();
    }

    private void keyboard(Component c, Object[] args) {
        // Expect at least: <latency> <part1> [<part2> ...]
        if (args.length < 2) {
            throw new RuntimeException("GCControlledInput keyboard requires at least 2 sub arguments: <latency-ms> <text>...");
        }

        // 1) Parse latency
        long latencyMs;
        Object first = args[0];
        if (first instanceof Number) {
            latencyMs = ((Number) first).longValue();
        } else if (first instanceof String) {
            try {
                latencyMs = Long.parseLong((String) first);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("First arg must be a number (latency in ms), parse failed. Got: " + first);
            }
        } else {
            throw new IllegalArgumentException("First arg must be a number (latency in ms), got: " + first);
        }

        // 2) Build full text from remaining args
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            sb.append(args[i].toString());
            if (i < args.length - 1) {
                sb.append(" ");
            }
        }
        String text = sb.toString();

        // 3) For each Unicode code point, post KEY_PRESSED, KEY_TYPED, KEY_RELEASED
        new Thread(() -> {
            try {
                System.out.println("GCControlledInput: typing \"" + text + "\" with latency " + latencyMs + "ms for each character.");
                for (int offset = 0; offset < text.length(); ) {
                    int codePoint = text.codePointAt(offset);
                    offset += Character.charCount(codePoint);

                    // Create and post KEY_PRESSED
                    KeyEvent press = new KeyEvent(
                            c,
                            KeyEvent.KEY_PRESSED,
                            System.currentTimeMillis(),
                            0,
                            KeyEvent.VK_UNDEFINED,              // Unknown virtual key
                            (char) codePoint
                    );
                    Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(press);

                    // Create and post KEY_TYPED
                    KeyEvent typed = new KeyEvent(
                            c,
                            KeyEvent.KEY_TYPED,
                            System.currentTimeMillis(),
                            0,
                            KeyEvent.VK_UNDEFINED,
                            (char) codePoint
                    );
                    Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(typed);

                    // Create and post KEY_RELEASED
                    KeyEvent release = new KeyEvent(
                            c,
                            KeyEvent.KEY_RELEASED,
                            System.currentTimeMillis(),
                            0,
                            KeyEvent.VK_UNDEFINED,
                            (char) codePoint
                    );
                    Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(release);

                    // Wait before next character
                    Thread.sleep(latencyMs);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted during synthetic typing", e);
            }
        }).start();
    }

    private void mouseClick(Component c, Object[] args) {
        try {
            Robot robot = new Robot();
            Point p = c.getLocationOnScreen();
            System.out.println("GCControlledInput: mouseClick at " + p.x + ", " + p.y + " on component " + c.getClass().getName());
            robot.mouseMove(p.x + c.getWidth() / 2, p.y + c.getHeight() / 2);
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        } catch (AWTException e) {
            throw new RuntimeException("GCControlledInput mouseClick failed: " + e.getMessage());
        }
    }
}

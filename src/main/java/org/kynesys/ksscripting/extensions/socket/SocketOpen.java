package org.kynesys.ksscripting.extensions.socket;

import lombok.Getter;
import org.kynesys.lwks.KSExecutionSession;

import org.kynesys.ksscripting.objects.KSScriptingNull;
import org.kynesys.lwks.KSScriptingExecutable;
import org.kynesys.lwks.extensions.KSSocket;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

@Getter
public class SocketOpen implements KSScriptingExecutable {

    private final String manual = "SocketOpen@me.hysong.atlas.cmdkit\n" +
            "This command will open a socket server that echos the incoming data." +
            "Usage: SocketOpen <listening port> <whitelist> [optional: action template object (implements KSSocket.AcceptAction)] [optional: authorization if KSSocket requires]";

    @Override
    public String returnType() {
        return KSScriptingNull.class.getName();
    }

    @Override
    public Object execute(Object[] args, KSExecutionSession session) throws Exception {
        // Check parameters
        if (args.length < 2) {
            throw new RuntimeException("Usage: SocketOpen <listening port> <whitelist> [optional: action template object (implements KSSocket.AcceptAction)] [optional: authorization if KSSocket requires]");
        }

        Object port = args[0];
        List<String> whitelist;

        if (args[1] instanceof List<?>) {
            whitelist = (List<String>) args[1];
        } else {
            throw new RuntimeException("Usage: SocketOpen <listening port> <whitelist> [optional: action template object (implements KSSocket.AcceptAction)] [optional: authorization if KSSocket requires]");
        }

        int realPort = -1;
        // Check if port is a number
        if (Number.class.isAssignableFrom(port.getClass())) {
            realPort = (int) port;
        } else {
            try {
                realPort = Integer.parseInt(port.toString());
            } catch (Exception e) {
                throw new RuntimeException("Port must be a number");
            }
        }

        KSSocket.AcceptAction action = (request, decodedPayload) -> {
            System.out.println(decodedPayload);
            return (Serializable) decodedPayload;
        };
        if (args.length >= 3) {
            if (args[2] instanceof KSSocket.AcceptAction) {
                action = (KSSocket.AcceptAction) args[2];
            }
        }

        KSSocket ssrv = new KSSocket(whitelist.toArray(new String[0]), realPort, action);
        ssrv.run();
        return new KSScriptingNull();
    }
}

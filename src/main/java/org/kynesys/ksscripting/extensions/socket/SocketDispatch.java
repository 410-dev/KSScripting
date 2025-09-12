package org.kynesys.ksscripting.extensions.socket;

import lombok.Getter;
import org.kynesys.lwks.KSExecutionSession;
import org.kynesys.lwks.KSScriptingExecutable;
import org.kynesys.lwks.extensions.KSSocketPayload;


import java.io.Serializable;

@Getter
public class SocketDispatch implements KSScriptingExecutable {

    private final String manual = "SocketDispatch@me.hysong.atlas.cmdkit\n" +
            "This command dispatches an object to open socket." +
            "Usage: SocketDispatch <host> <port> <serializable object> [optional: authorization if KSSocket requires]";

    @Override
    public String returnType() {
        return Object.class.getName();
    }

    @Override
    public Object execute(Object[] args, KSExecutionSession session) throws Exception {
        // Check parameters
        if (args.length < 3) {
            throw new RuntimeException("Usage: SocketDispatch <host> <port> <serializable object> [optional: authorization if KSSocket requires]");
        }

        String host = args[0].toString();
        Object port = args[1];
        Object object = args[2];
        String authorization = args.length == 4 ? args[3].toString() : "";
        int realPort = -1;

        // Check if implements serializable
        if (!(Serializable.class.isAssignableFrom(object.getClass()))) {
            throw new RuntimeException("Object must implement Serializable");
        }
        Serializable serializableObject = (Serializable) object;

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

        // Run socket connection
        KSSocketPayload payload = new KSSocketPayload(serializableObject, host, realPort, authorization);
        return payload.dispatchSync();
    }
}

package org.kynesys.lwks.extensions;


import lombok.Getter;
import lombok.Setter;
import org.kynesys.lwks.ParameteredRunnable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;

@Getter
@Setter
public class KSSocketPayload implements Serializable {
    private Serializable payload;
    private String host;
    private int port;
    private String authorization;

    public KSSocketPayload(Serializable payload, String host, int port, String authorization) {
        this.payload = payload;
        this.host = host;
        this.port = port;
        this.authorization = authorization;
    }

    public KSSocketPayload() {}

    public Object dispatchSync() throws IOException {

        try (SocketChannel ch = SocketChannel.open(new InetSocketAddress(host, port));
             ObjectOutputStream out = new ObjectOutputStream(Channels.newOutputStream(ch));
             ObjectInputStream in = new ObjectInputStream(Channels.newInputStream(ch))) {

            out.writeObject(this);
            out.flush();
            return in.readObject();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void dispatchAsync() {
        dispatchAsync(null, null);
    }

    public void dispatchAsync(ParameteredRunnable success) {
        dispatchAsync(success, null);
    }

    public void dispatchAsync(ParameteredRunnable success, ParameteredRunnable fail) {
        new Thread(() -> {
            try {
                Object response = dispatchSync();
                if (success != null) {
                    success.run(response);
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (fail != null) {
                    fail.run(e);
                }
            }
        }).start();
    }
}
package org.kynesys.lwks.extensions;

import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.UUID;

public class KSSocket implements Runnable {
    private final String[] whitelistHosts;
    private final int port;
    private final AcceptAction action;
    private final Authorization authorization;

    @Setter
    @Getter
    private boolean allowNonKSSocketPayload = false;

    private volatile ServerSocketChannel serverChannel;
    private volatile Thread acceptThread;

    public KSSocket(String[] whitelistHosts, int port, AcceptAction action, Authorization authorization) {
        this.whitelistHosts = whitelistHosts;
        this.port = port;
        this.action = action;
        this.authorization = authorization;
    }

    public KSSocket(String[] whitelistHosts, int port, AcceptAction action) {
        this(whitelistHosts, port, action, auth -> true);
    }

    public KSSocket(String whitelistHost, int port, AcceptAction action) {
        this(new String[]{whitelistHost}, port, action);
    }

    @Override
    public void run() {
        acceptThread = Thread.currentThread();
        try {
            serverChannel = ServerSocketChannel.open();
            serverChannel.bind(new InetSocketAddress(port));
            System.out.println("Listening on port " + port);

            while (!acceptThread.isInterrupted()) {
                SocketChannel clientChannel;
                try {
                    clientChannel = serverChannel.accept();  // blocking, interruptible
                } catch (ClosedByInterruptException e) {
                    System.out.println("Server interrupted, shutting down.");
                    break;
                } catch (IOException e) {
                    e.printStackTrace();
                    continue;
                }

                InetSocketAddress remote = (InetSocketAddress) clientChannel.getRemoteAddress();
                String clientIp = remote.getAddress().getHostAddress();

                if (whitelistHosts.length > 0 && !Arrays.asList(whitelistHosts).contains(clientIp)) {
                    System.out.println("Rejected connection from " + clientIp);
                    try {
                        clientChannel.close();
                    } catch (IOException ignored) {
                    }
                    continue;
                }

                System.out.println("Accepted connection from " + clientIp);
                new Thread(() -> {
                    try {
                        handle(clientChannel.socket());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, "KSSocket-Worker-" + UUID.randomUUID()).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            cleanup();
        }
    }

    public Thread startThread() {
        Thread t = new Thread(this, "KSSocket-Acceptor-" + UUID.randomUUID());
        t.start();
        return t;
    }

    /**
     * Request the server to stop: closes the channel and interrupts acceptor.
     */
    public void shutdown() {
        if (acceptThread != null) {
            acceptThread.interrupt();
        }
        if (serverChannel != null) {
            try {
                serverChannel.close();
            } catch (IOException ignored) {
            }
        }
    }

    private void cleanup() {
        System.out.println("Cleaning up server resources.");
        serverChannel = null;
        acceptThread = null;
    }

    private void handle(Socket client) throws IOException, ClassNotFoundException {
        try (
                ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(client.getInputStream())
        ) {
            out.flush();  // send stream header

            while (true) {
                Object obj = in.readObject();
                Request req;
                if (obj instanceof KSSocketPayload payload) {
                    if (authorization == null || !authorization.check(payload.getAuthorization())) {
                        out.writeObject("error:unauthorized");
                        out.flush();
                        continue;
                    }
                    req = new Request(client, payload.getPayload());
                } else {
                    if (!allowNonKSSocketPayload) {
                        out.writeObject("error:invalid_input_type");
                        out.flush();
                        continue;
                    }
                    req = new Request(client, obj);
                }

                Serializable response = action.run(req, req.getDecodedPayload());
                out.writeObject(response);
                out.flush();
            }
        } catch (IOException e) {
            // client disconnected or stream error
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                client.close();
            } catch (IOException ignored) {
            }
        }
    }

    @FunctionalInterface
    public interface AcceptAction {
        Serializable run(Request request, Object decodedPayload) throws IOException;
    }

    @FunctionalInterface
    public interface Authorization {
        boolean check(String authorizationString);
    }

    @Getter
    public static class Request {
        private final Socket client;
        private final Object decodedPayload;
        private final String clientIp;
        private final int port;

        public Request(Socket socket, Object decodedPayload) {
            this.client = socket;
            this.decodedPayload = decodedPayload;
            InetSocketAddress rem = (InetSocketAddress) socket.getRemoteSocketAddress();
            this.clientIp = rem.getAddress().getHostAddress();
            this.port = rem.getPort();
        }

        public Request(Socket socket, @SuppressWarnings("unused") Object decodedPayload, String clientIp, int port) {
            this.client = socket;
            this.decodedPayload = decodedPayload;
            this.clientIp = clientIp;
            this.port = port;
        }
    }
}
package im.tox.tox4j;

import im.tox.tox4j.annotations.NotNull;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SocksServer implements Closeable, Runnable {

    private final ServerSocket server;
    private final List<Thread> threads = new ArrayList<>();
    private final List<Socket> sockets = new ArrayList<>();
    private boolean running = true;
    private int accepted = 0;

    public SocksServer() throws IOException {
        ServerSocket server = null;
        IOException lastException = null;
        for (int port = 8000; port < 9000; port++) {
            try {
                server = new ServerSocket(port);
                break;
            } catch (IOException e) {
                lastException = e;
            }
        }
        if (server == null) {
            throw lastException;
        }
        this.server = server;
    }

    @Override
    public void close() throws IOException {
        running = false;
        server.close();
    }

    public int getPort() {
        return server.getLocalPort();
    }

    public @NotNull String getAddress() {
        return server.getInetAddress().getHostAddress();
    }

    @Override
    public void run() {
        try {
            while (running) {
                final Socket socket = server.accept();
                sockets.add(socket);

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
//                        System.out.println("Accepted connection from " + socket.getInetAddress());
                        try (InputStream input = socket.getInputStream()) {
                            try (OutputStream output = socket.getOutputStream()) {
                                greeting(input, output);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

                thread.start();
                threads.add(thread);
                accepted++;
            }
        } catch (IOException abort) {
            running = false;
            try {
                server.close();
                for (Socket socket : sockets) {
                    socket.close();
                }
                for (Thread thread : threads) {
                    thread.join();
                }
            } catch (InterruptedException | IOException error) {
                error.printStackTrace();
            }
        }
    }

    private void greeting(InputStream input, OutputStream output) throws IOException {
        // SOCKS5
        assertEquals(0x05, input.read());

        // Client sends authentication methods supported.
        int numAuthenticationMethods = input.read();
        int[] authenticationMethods = new int[numAuthenticationMethods];
        for (int i = 0; i < numAuthenticationMethods; i++) {
            authenticationMethods[i] = input.read();
        }

        // Choose an authentication method we support (none)
        for (int method : authenticationMethods) {
            if (method == 0x00) {
                // Great! no authentication, send back our choice
                output.write(0x05); // socks5
                output.write(0x00); // no authentication
                // Done. Now wait for the connection request.
                connection(input, output);
                return;
            }
        }

        // No supported authentication method.
        throw new IOException("Client did not support any of our authentication methods");
    }

    private void connection(@NotNull InputStream input, @NotNull OutputStream output) throws IOException {
        assertEquals(0x05, input.read());
        int command = input.read();
        assertEquals(0x00, input.read());

        InetAddress address;
        switch (input.read()) {
            case 0x01:
                // IPv4 address
                byte[] address4 = new byte[4];
                assertEquals(address4.length, input.read(address4));
                address = InetAddress.getByAddress(address4);
                break;
            case 0x03:
                // Domain name
                byte[] domain = new byte[input.read()];
                assertEquals(domain.length, input.read(domain));
                address = InetAddress.getByName(new String(domain));
                break;
            case 0x04:
                // IPv6 address
                byte[] address6 = new byte[16];
                assertEquals(address6.length, input.read(address6));
                address = InetAddress.getByAddress(address6);
                break;
            default:
                throw new IOException("Unsupported address type");
        }

        byte[] portBytes = new byte[2];
        assertEquals(2, input.read(portBytes));
        int port = ((portBytes[0] & 0xff) << 8) | (portBytes[1] & 0xff);

        switch (command) {
            case 0x01:
                establishStream(input, output, address, port);
                break;
            case 0x02:
                throw new IOException("TCP/IP port binding not supported");
            case 0x03:
                throw new IOException("Associating UDP port not supported");
            default:
                throw new IOException("Unknown command: " + command);
        }
    }

    private void establishStream(InputStream input, final OutputStream output, InetAddress address, int port) throws IOException {
        output.write(0x05); // socks5
        Socket target;
        try {
            target = new Socket(address, port);
            sockets.add(target);
        } catch (IOException e) {
            if (e.getMessage().equals("Network is unreachable")) {
//                System.err.println("Network unreachable for address " + address);
                output.write(0x03); // network unreachable
                return;
            } else {
                throw e;
            }
        }
        // OK, we accept it (we accept everything).
        output.write(0x00); // accept
        output.write(0x00); // reserved
        byte[] addressBytes = address.getAddress();
        if (addressBytes.length == 4) {
            output.write(0x01); // ipv4
        } else {
            assertEquals(16, addressBytes.length);
            output.write(0x04); // ipv6
        }
        output.write(addressBytes);
        output.write(port >> 8);
        output.write(port & 0xff);

        // Start piping the two streams.
        final InputStream targetInput = target.getInputStream();
        OutputStream targetOutput = target.getOutputStream();

        Thread inputThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int inByte;
                    while ((inByte = targetInput.read()) != -1) {
                        output.write(inByte);
                    }
                } catch (IOException ignored) {
                    // Socket closed. We're done.
                }
            }
        });
        inputThread.start();
        threads.add(inputThread);

        try {
            int outByte;
            while ((outByte = input.read()) != -1) {
                targetOutput.write(outByte);
            }
        } catch (IOException ignored) {
            // Socket closed. We're done.
        }
    }

    public int getAccepted() {
        return accepted;
    }
}

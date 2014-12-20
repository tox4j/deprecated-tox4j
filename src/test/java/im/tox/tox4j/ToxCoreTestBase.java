package im.tox.tox4j;

import im.tox.tox4j.callbacks.ConnectionStatusCallback;
import im.tox.tox4j.enums.ToxConnection;
import im.tox.tox4j.enums.ToxProxyType;
import im.tox.tox4j.exceptions.ToxBootstrapException;
import im.tox.tox4j.exceptions.ToxFriendAddException;
import im.tox.tox4j.exceptions.ToxNewException;
import org.junit.Assume;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;

import static org.junit.Assume.assumeNotNull;
import static org.junit.Assume.assumeTrue;

public abstract class ToxCoreTestBase {

    protected static final boolean LOGGING = true;
    protected static final int TIMEOUT = 40000;
    protected static final int ITERATIONS = 500;

    protected static class DhtNode {
        protected final String ipv4;
        protected final String ipv6;
        protected final int port;
        protected final byte[] dhtId;

        public DhtNode(String ipv4, String ipv6, int port, String dhtId) {
            this.ipv4 = ipv4;
            this.ipv6 = ipv6;
            this.port = port;
            this.dhtId = parseClientId(dhtId);
        }
    }

    protected static final DhtNode[] nodes = {
            // sonOfRa
            new DhtNode(
                    "144.76.60.215",
                    "2a01:4f8:191:64d6::1",
                    33445,
                    "04119E835DF3E78BACF0F84235B300546AF8B936F035185E2A8E9E0A67C8924F"
            ),
            // stqism
            new DhtNode(
                    "192.254.75.98",
                    "2607:5600:284::2",
                    33445,
                    "FE3914F4616E227F29B2103450D6B55A836AD4BD23F97144E2C4ABE8D504FE1B"
            ),
    };

    protected abstract ToxCore newTox(ToxOptions options) throws ToxNewException;

    protected ToxCore newTox() throws ToxNewException {
        return newTox(new ToxOptions());
    }

    protected ToxCore newTox(boolean ipv6Enabled, boolean udpEnabled) throws ToxNewException {
        ToxOptions options = new ToxOptions();
        options.setIpv6Enabled(ipv6Enabled);
        options.setUdpEnabled(udpEnabled);
        return newTox(options);
    }

    protected ToxCore newTox(boolean ipv6Enabled, boolean udpEnabled, ToxProxyType proxyType, String proxyAddress, int proxyPort) throws ToxNewException {
        ToxOptions options = new ToxOptions();
        options.setIpv6Enabled(ipv6Enabled);
        options.setUdpEnabled(udpEnabled);
        options.enableProxy(proxyType, proxyAddress, proxyPort);
        return newTox(options);
    }

    protected static class ConnectedListener implements ConnectionStatusCallback {
        private ToxConnection value = ToxConnection.NONE;

        @Override
        public void connectionStatus(ToxConnection connectionStatus) {
            value = connectionStatus;
        }

        public boolean isConnected() {
            return value != ToxConnection.NONE;
        }
    }

    protected class ToxList implements Closeable {
        private final ToxCore[] toxes;
        private final ToxConnection[] connected;

        public ToxList(int count) throws ToxNewException {
            this.toxes = new ToxCore[count];
            this.connected = new ToxConnection[toxes.length];
            for (int i = 0; i < count; i++) {
                final int id = i;
                toxes[i] = newTox();
                toxes[i].callbackConnectionStatus(new ConnectionStatusCallback() {
                    @Override
                    public void connectionStatus(ToxConnection connectionStatus) {
                        connected[id] = connectionStatus;
                    }
                });
            }
        }

        @Override
        public void close() throws IOException {
            for (ToxCore tox : toxes) {
                tox.close();
            }
        }

        public boolean isAllConnected() {
            boolean result = true;
            for (ToxConnection tox : connected) {
                result = result && tox != ToxConnection.NONE;
            }
            return result;
        }

        public boolean isAnyConnected() {
            for (ToxConnection tox : connected) {
                if (tox != ToxConnection.NONE) {
                    return true;
                }
            }
            return false;
        }

        public void iteration() {
            for (ToxCore tox : toxes) {
                tox.iteration();
            }
        }

        public int iterationInterval() {
            int result = 0;
            for (ToxCore tox : toxes) {
                result = Math.max(result, tox.iterationInterval());
            }
            return result;
        }

        public ToxCore get(int index) {
            return toxes[index];
        }

        public int size() {
            return toxes.length;
        }
    }


    protected static double entropy(byte[] data) {
        int[] frequencies = new int[256];
        for (byte b : data) {
            frequencies[127 - b]++;
        }

        double entropy = 0;
        for (int frequency : frequencies) {
            if (frequency != 0) {
                double probability = (double)frequency / data.length;
                entropy -= probability * (Math.log(probability) / Math.log(256));
            }
        }

        return entropy;
    }


    protected static byte[] randomBytes(int length) {
        byte[] array = new byte[length];
        new Random().nextBytes(array);
        return array;
    }


    // return one of the friends (the last one)
    protected int addFriends(ToxCore tox, int count) throws ToxNewException, ToxFriendAddException {
        if (count < 1) {
            throw new IllegalArgumentException("Cannot add less than 1 friend: " + count);
        }
        int friendNumber = -1;
        byte[] message = "heyo".getBytes();
        for (int i = 0; i < count; i++) {
            try (ToxCore friend = newTox()) {
                friendNumber = tox.addFriend(friend.getAddress(), message);
            }
        }
        return friendNumber;
    }

    private static byte[] parseClientId(String id) {
        byte[] clientId = new byte[ToxConstants.CLIENT_ID_SIZE];
        for (int i = 0; i < ToxConstants.CLIENT_ID_SIZE; i++) {
            clientId[i] = (byte) (
                    (fromHexDigit(id.charAt(i * 2)) << 4) +
                            (fromHexDigit(id.charAt(i * 2 + 1)))
            );
        }
        return clientId;
    }

    private static byte fromHexDigit(char c) {
        if (c >= '0' && c <= '9') {
            return (byte)(c - '0');
        } else if (c >= 'A' && c <= 'F') {
            return (byte)(c - 'A' + 10);
        } else {
            throw new IllegalArgumentException("Non-hex digit character: " + c);
        }
    }

    protected static void assumeIPv6() {
        try {
            Socket socket = new Socket(InetAddress.getByName(nodes[0].ipv6), nodes[0].port);
            assumeNotNull(socket.getInputStream());
        } catch (IOException e) {
            assumeTrue("An IPv6 network connection can be established", false);
        }
    }

    protected ToxCore bootstrap(boolean useIPv6, ToxCore tox) throws ToxBootstrapException {
        if (useIPv6) {
            tox.bootstrap(nodes[0].ipv6, nodes[0].port, nodes[0].dhtId);
        } else {
            tox.bootstrap(nodes[0].ipv4, nodes[0].port, nodes[0].dhtId);
        }
        return tox;
    }

}

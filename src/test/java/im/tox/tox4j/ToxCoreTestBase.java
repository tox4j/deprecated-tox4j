package im.tox.tox4j;

import im.tox.tox4j.callbacks.ConnectionStatusCallback;
import im.tox.tox4j.enums.ToxConnection;
import im.tox.tox4j.enums.ToxProxyType;
import im.tox.tox4j.exceptions.ToxBootstrapException;
import im.tox.tox4j.exceptions.ToxFriendAddException;
import im.tox.tox4j.exceptions.ToxNewException;

import java.io.Closeable;
import java.io.IOException;
import java.util.Random;

public abstract class ToxCoreTestBase {

    protected static final boolean LOGGING = true;
    protected static final int TIMEOUT = 20000;
    protected static final int ITERATIONS = 500;

    protected static final String bootstrapValidIP = "144.76.60.215";
    protected static final int bootstrapValidPort = 33445;
    protected static final byte[] bootstrapValidDhtId =
            parseClientId("04119E835DF3E78BACF0F84235B300546AF8B936F035185E2A8E9E0A67C8924F");


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

    protected ToxCore bootstrap(ToxCore tox) throws ToxBootstrapException {
        tox.bootstrap(bootstrapValidIP, bootstrapValidPort, bootstrapValidDhtId);
        return tox;
    }

}

package im.tox.tox4j;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.core.ToxConstants;
import im.tox.tox4j.core.ToxCore;
import im.tox.tox4j.core.ToxOptions;
import im.tox.tox4j.core.callbacks.ConnectionStatusCallback;
import im.tox.tox4j.core.enums.ToxConnection;
import im.tox.tox4j.core.enums.ToxProxyType;
import im.tox.tox4j.core.exceptions.ToxBootstrapException;
import im.tox.tox4j.core.exceptions.ToxFriendAddException;
import im.tox.tox4j.core.exceptions.ToxNewException;
import org.easetech.easytest.annotation.Parallel;
import org.easetech.easytest.runner.DataDrivenTestRunner;
import org.junit.runner.RunWith;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Random;

import static org.junit.Assume.assumeNotNull;
import static org.junit.Assume.assumeTrue;

@RunWith(DataDrivenTestRunner.class)
@Parallel()
public abstract class ToxCoreTestBase {

    public static final int TIMEOUT = 60000;

    protected static final int ITERATIONS = 500;

    protected static class DhtNode {
        public final String ipv4;
        public final String ipv6;
        public final int port;
        public final byte[] dhtId;

        public DhtNode(String ipv4, String ipv6, int port, String dhtId) {
            this.ipv4 = ipv4;
            this.ipv6 = ipv6;
            this.port = port;
            this.dhtId = parseClientId(dhtId);
        }
    }

    static final DhtNode[] nodeCandidates = {
        // sonOfRa
        new DhtNode("144.76.60.215", "2a01:4f8:191:64d6::1", 33445, "04119E835DF3E78BACF0F84235B300546AF8B936F035185E2A8E9E0A67C8924F"),
        // stqism
        new DhtNode("192.254.75.98", "2607:5600:284::2", 33445, "951C88B7E75C867418ACDB5D273821372BB5BD652740BCDF623A4FA293E75D2F"),
        // others
        new DhtNode("37.187.46.132", null, 33445, "A9D98212B3F972BD11DA52BEB0658C326FCCC1BFD49F347F9C2D3D8B61E1B927"),
        new DhtNode("23.226.230.47", null, 33445, "A09162D68618E742FFBCA1C2C70385E6679604B2D80EA6E84AD0996A1AC8A074"),
        new DhtNode("54.199.139.199", null, 33445, "7F9C31FE850E97CEFD4C4591DF93FC757C7C12549DDD55F8EEAECC34FE76C029"),
        new DhtNode("192.210.149.121", null, 33445, "F404ABAA1C99A9D37D61AB54898F56793E1DEF8BD46B1038B9D822E8460FAB67"),
        new DhtNode("37.59.102.176", null, 33445, "B98A2CEAA6C6A2FADC2C3632D284318B60FE5375CCB41EFA081AB67F500C1B0B"),
        new DhtNode("178.21.112.187", null, 33445, "4B2C19E924972CB9B57732FB172F8A8604DE13EEDA2A6234E348983344B23057"),
        new DhtNode("107.161.17.51", null, 33445, "7BE3951B97CA4B9ECDDA768E8C52BA19E9E2690AB584787BF4C90E04DBB75111"),
        new DhtNode("31.7.57.236", null, 443, "2A4B50D1D525DA2E669592A20C327B5FAD6C7E5962DC69296F9FEC77C4436E4E"),
        new DhtNode("63.165.243.15", null, 443, "8CD087E31C67568103E8C2A28653337E90E6B8EDA0D765D57C6B5172B4F1F04C"),
    };

    protected abstract @NotNull DhtNode node();

    protected abstract @NotNull
    ToxCore newTox(ToxOptions options, byte[] data) throws ToxNewException;

    protected final @NotNull ToxCore newTox() throws ToxNewException {
        return newTox(new ToxOptions(), null);
    }

    protected final @NotNull ToxCore newTox(byte[] data) throws ToxNewException {
        return newTox(new ToxOptions(), data);
    }

    protected final @NotNull ToxCore newTox(ToxOptions options) throws ToxNewException {
        return newTox(options, null);
    }

    protected final @NotNull ToxCore newTox(boolean ipv6Enabled, boolean udpEnabled) throws ToxNewException {
        ToxOptions options = new ToxOptions();
        options.setIpv6Enabled(ipv6Enabled);
        options.setUdpEnabled(udpEnabled);
        return newTox(options, null);
    }

    protected final @NotNull ToxCore newTox(boolean ipv6Enabled, boolean udpEnabled, ToxProxyType proxyType, String proxyAddress, int proxyPort) throws ToxNewException {
        ToxOptions options = new ToxOptions();
        options.setIpv6Enabled(ipv6Enabled);
        options.setUdpEnabled(udpEnabled);
        options.enableProxy(proxyType, proxyAddress, proxyPort);
        return newTox(options, null);
    }

    protected static class ToxList implements Closeable {
        private final @NotNull ToxCore[] toxes;
        private final @NotNull ToxConnection[] connected;

        public ToxList(ToxCoreTestBase factory, int count) throws ToxNewException {
            this.toxes = new ToxCore[count];
            this.connected = new ToxConnection[toxes.length];
            for (int i = 0; i < count; i++) {
                final int id = i;
                connected[i] = ToxConnection.NONE;
                toxes[i] = factory.newTox();
                toxes[i].callbackConnectionStatus(new ConnectionStatusCallback() {
                    @Override
                    public void connectionStatus(@NotNull ToxConnection connectionStatus) {
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
            for (ToxConnection connectionStatus : connected) {
                result = result && connectionStatus != ToxConnection.NONE;
            }
            return result;
        }

        public boolean isAnyConnected() {
            for (ToxConnection connectionStatus : connected) {
                if (connectionStatus != ToxConnection.NONE) {
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


    static double entropy(@NotNull byte[] data) {
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


    protected static @NotNull byte[] randomBytes(int length) {
        byte[] array = new byte[length];
        new Random().nextBytes(array);
        return array;
    }


    // return one of the friends (the last one)
    protected int addFriends(@NotNull ToxCore tox, int count) throws ToxNewException, ToxFriendAddException {
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

    private static byte[] parseClientId(@NotNull String id) {
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

    protected void assumeIPv4() {
        try (Socket socket = new Socket(InetAddress.getByName(node().ipv4), node().port)) {
            assumeNotNull(socket.getInputStream());
        } catch (IOException e) {
            assumeTrue("An IPv4 network connection can be established", false);
        }
    }

    protected void assumeIPv6() {
        try (Socket socket = new Socket(InetAddress.getByName(node().ipv6), node().port)) {
            assumeNotNull(socket.getInputStream());
        } catch (IOException e) {
            assumeTrue("An IPv6 network connection can be established", false);
        }
    }

    @NotNull ToxCore bootstrap(boolean useIPv6, @NotNull ToxCore tox) throws ToxBootstrapException {
        if (useIPv6) {
            tox.bootstrap(node().ipv6, node().port, node().dhtId);
        } else {
            tox.bootstrap(node().ipv4, node().port, node().dhtId);
        }
        return tox;
    }

}

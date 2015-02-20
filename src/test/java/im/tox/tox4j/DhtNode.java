package im.tox.tox4j;

public class DhtNode {
    public final String ipv4;
    public final String ipv6;
    public final int port;
    public final byte[] dhtId;

    public DhtNode(String ipv4, String ipv6, int port, String dhtId) {
        this.ipv4 = ipv4;
        this.ipv6 = ipv6;
        this.port = port;
        this.dhtId = ToxCoreTestBase.parsePublicKey(dhtId);
    }
}

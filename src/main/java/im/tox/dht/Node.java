package im.tox.dht;

import java.net.Inet4Address;
import java.net.Inet6Address;

public class Node {
    private final String owner;
    private final byte[] publicKey;
    private final Inet4Address ipv4;
    private final Inet6Address ipv6;
    private final int port;

    public Node(String owner, byte[] publicKey, Inet4Address ipv4, Inet6Address ipv6, int port) {
        this.owner = owner;
        this.publicKey = publicKey;
        this.ipv4 = ipv4;
        this.ipv6 = ipv6;
        this.port = port;
    }

    public String getOwner() {
        return owner;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public Inet4Address getIpv4() {
        return ipv4;
    }

    public Inet6Address getIpv6() {
        return ipv6;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return "Node{" +
                "owner='" + owner + '\'' +
                ", publicKey='" + publicKey + '\'' +
                ", ipv4='" + ipv4 + '\'' +
                ", ipv6='" + ipv6 + '\'' +
                ", port=" + port +
                '}';
    }
}

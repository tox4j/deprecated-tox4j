package im.tox.dht.parser;

import im.tox.dht.Node;
import im.tox.dht.NodeValidator;
import im.tox.dht.ParseException;
import im.tox.tox4j.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.util.ArrayList;
import java.util.List;

public class JsonNodeParser implements NodeParser {
    private static final String SERVERS = "servers";
    private static final String OWNER = "owner";
    private static final String PUBLIC_KEY = "pubkey";
    private static final String IPV4 = "ipv4";
    private static final String IPV6 = "ipv6";
    private static final String PORT = "port";

    @Override
    public List<Node> parse(String json) throws ParseException {
        JSONObject object = new JSONObject(json);
        return parseRootObject(object);
    }

    private static List<Node> parseRootObject(JSONObject root) {
        List<Node> nodes = new ArrayList<>();
        JSONArray serverNodes = root.getJSONArray(SERVERS);
        for (int i = 0; i < serverNodes.length(); i++) {
            try {
                Node node = parseNode((JSONObject) serverNodes.get(i));
                if (NodeValidator.isValid(node)) {
                    nodes.add(node);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return nodes;
    }

    private static Node parseNode(JSONObject jsonObject) throws Exception {
        String owner = jsonObject.optString(OWNER, null);
        String publicKey = jsonObject.getString(PUBLIC_KEY);
        String ipv4Value = jsonObject.getString(IPV4);
        String ipv6Value = jsonObject.optString(IPV6, null);
        int port = jsonObject.getInt(PORT);

        Inet4Address ipv4 = null;
        try {
            ipv4 = (Inet4Address) Inet4Address.getByName(ipv4Value);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Inet6Address ipv6 = null;
        try {
            ipv6 = ipv6Value == null ? null : (Inet6Address) Inet6Address.getByName(ipv6Value);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new Node(owner, hexStringToByteArray(publicKey), ipv4, ipv6, port);
    }

    private static byte[] hexStringToByteArray(@NotNull String value) {
        int len = value.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(value.charAt(i), 16) << 4) + Character.digit(value.charAt(i+1), 16));
        }
        return data;
    }
}

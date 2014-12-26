package im.tox.dht;

import im.tox.tox4j.ToxConstants;

import java.util.ArrayList;
import java.util.List;

public class NodeValidator {

    public static boolean isValid(Node node) {
        boolean valid;

        valid = node.getPort() > 0 && node.getPort() < 65536;

        valid &= node.getPublicKey() != null && node.getPublicKey().length == ToxConstants.CLIENT_ID_SIZE;

        valid &= node.getIpv4() != null;

        return valid;
    }

    public static List<Node> getValidNodes(List<Node> nodes) {
        List<Node> validNodes = new ArrayList<>();

        for (Node node : nodes) {
            if (NodeValidator.isValid(node)) {
                validNodes.add(node);
            }
        }

        return validNodes;
    }
}

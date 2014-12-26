package im.tox.dht;

import im.tox.dht.fetcher.NodeFetcher;
import im.tox.dht.parser.NodeParser;
import im.tox.tox4j.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class NodeProvider {

    public static @NotNull List<Node> getNodes(@NotNull NodeFetcher fetcher, @NotNull NodeParser parser) {
        try {
            return parser.parse(fetcher.getJson());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
}

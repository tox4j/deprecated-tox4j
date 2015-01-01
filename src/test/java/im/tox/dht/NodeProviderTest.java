package im.tox.dht;

import im.tox.dht.fetcher.NodeFetcher;
import im.tox.dht.fetcher.StringNodeFetcher;
import im.tox.dht.parser.JsonNodeParser;
import im.tox.dht.parser.NodeParser;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class NodeProviderTest {

    private static final NodeParser parser = new JsonNodeParser();

    private static List<Node> getNodes(String json) {
        NodeFetcher fetcher = new StringNodeFetcher(json);
        return NodeProvider.getNodes(fetcher, parser);
    }

    @Test
    public void testEmptyString() throws Exception {
        assertThat(getNodes(""), is(empty()));
    }

    @Test
    public void testEmptyBraces() throws Exception {
        assertThat(getNodes("{}"), is(empty()));
    }

    @Test
    public void testEmptyServers() throws Exception {
        assertThat(getNodes("{servers:[]}"), is(empty()));
    }

    @Test
    public void testServersObject() throws Exception {
        assertThat(getNodes("{servers:{}}"), is(empty()));
    }

    @Test
    public void testEmptyServer() throws Exception {
        assertThat(getNodes("{servers:[{}]}"), is(empty()));
    }

    @Test
    public void testOneMember() throws Exception {
        assertEquals(1, getNodes("{servers:[{pubkey:'2C308B4518862740AD9A121598BCA7713AFB25858B747313A4D073E2F6AC506C', ipv4:'127.0.0.1', port:1, owner:'', ipv6:'::1'}]}").size());
    }

}
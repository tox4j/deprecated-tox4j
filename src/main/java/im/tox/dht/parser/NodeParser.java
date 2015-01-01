package im.tox.dht.parser;

import im.tox.dht.Node;
import im.tox.dht.ParseException;

import java.util.List;

public interface NodeParser {

    List<Node> parse(String data) throws ParseException;
}

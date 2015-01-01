package im.tox.dht.fetcher;

public class StringNodeFetcher implements NodeFetcher {
    private final String json;

    public StringNodeFetcher(String json) {
        this.json = json;
    }

    @Override
    public String getJson() {
        return json;
    }
}

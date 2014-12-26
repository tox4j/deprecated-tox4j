package im.tox.dht.fetcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class FileNodeFetcher implements NodeFetcher {
    private final File nodeFile;

    public FileNodeFetcher(File file) {
        this.nodeFile = file;
    }

    @Override
    public String getJson() {
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            try (BufferedReader br = new BufferedReader(new FileReader(nodeFile))) {
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                return sb.toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}

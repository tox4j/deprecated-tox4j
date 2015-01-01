package im.tox.dht.fetcher;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class UrlNodeFetcher implements NodeFetcher {
    private final URL nodeURL;

    public UrlNodeFetcher(URL nodeURL) {
        this.nodeURL = nodeURL;
    }

    @Override
    public String getJson() {
        SSLSocketFactory sslSocketFactory = HttpsURLConnection.getDefaultSSLSocketFactory();

        try {
            trustAllCertificates();

            StringBuilder sb = new StringBuilder();
            String line;

            BufferedReader br = new BufferedReader(new InputStreamReader(nodeURL.openStream()));
            try {
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
            } finally {
                br.close();
            }
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            HttpsURLConnection.setDefaultSSLSocketFactory(sslSocketFactory);
        }

        return null;
    }

    private static void trustAllCertificates() throws IOException {
        // Create a new trust manager that trust all certificates
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
        };

        // Activate the new trust manager
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
        }
    }
}

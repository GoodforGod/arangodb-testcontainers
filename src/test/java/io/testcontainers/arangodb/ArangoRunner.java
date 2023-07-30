package io.testcontainers.arangodb;

import io.testcontainers.arangodb.containers.ArangoContainer;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import org.junit.jupiter.api.Assertions;

/**
 * Description in progress
 *
 * @author Anton Kurako (GoodforGod)
 * @since 4.3.2020
 */
public abstract class ArangoRunner extends Assertions {

    protected static final String IMAGE_3_7 = "arangodb:3.7.13";
    protected static final String IMAGE_3_11 = "arangodb:3.11.2";

    protected URL getCheckUrl(ArangoContainer<?> container) {
        try {
            return new URL("http://" + container.getHost() + ":" + container.getPort() + "/_api/collection");
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    protected String getResponse(HttpURLConnection connection) {
        final StringBuilder builder = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                builder.append(inputLine);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }

        return builder.toString();
    }
}

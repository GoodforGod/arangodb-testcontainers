package io.testcontainers.arangodb.containers;

import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * ArangoDB TestContainer tests.
 *
 * @author Anton Kurako (GoodforGod)
 * @since 2.3.2020
 */
@Testcontainers
class ArangoContainerSetPortTests extends ArangoRunner {

    private static final int PORT = 8233;

    @Container
    private static final ArangoContainer container = new ArangoContainer().withoutAuth().withPort(PORT);

    @Test
    void checkThatDatabaseIsRunning() throws Exception {
        final boolean running = container.isRunning();
        assertTrue(running);

        final URL url = getCheckUrl(container);
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        connection.connect();

        final int status = connection.getResponseCode();
        final String response = getResponse(connection);

        assertEquals(200, status);
        assertFalse(response.isEmpty());
    }
}

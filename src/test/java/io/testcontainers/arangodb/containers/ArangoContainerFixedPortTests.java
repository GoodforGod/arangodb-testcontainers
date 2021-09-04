package io.testcontainers.arangodb.containers;

import static io.testcontainers.arangodb.containers.ArangoContainer.LATEST;

import io.testcontainers.arangodb.ArangoRunner;
import java.net.HttpURLConnection;
import java.net.URL;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * ArangoDB TestContainer tests.
 *
 * @author Anton Kurako (GoodforGod)
 * @since 2.3.2020
 */
@Testcontainers
class ArangoContainerFixedPortTests extends ArangoRunner {

    private static final int PORT = 8233;

    @Container
    private static final ArangoContainer container = new ArangoContainer(LATEST).withoutAuth().withFixedPort(PORT);

    @Test
    void checkThatDatabaseIsRunning() throws Exception {
        final boolean running = container.isRunning();
        assertTrue(running);

        final URL url = getCheckUrl(container);
        assertEquals(PORT, container.getPort());
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

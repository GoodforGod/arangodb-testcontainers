package io.testcontainers.arangodb.containers;

import io.testcontainers.arangodb.ArangoRunner;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.net.HttpURLConnection;
import java.net.URL;

import static io.testcontainers.arangodb.containers.ArangoContainer.LATEST;

/**
 * ArangoDB TestContainer tests.
 *
 * @author Anton Kurako (GoodforGod)
 * @since 2.3.2020
 */
@Testcontainers
class ArangoContainerSetPasswordTests extends ArangoRunner {

    private static final String PASS = "mypass";

    @Container
    private static final ArangoContainer container = new ArangoContainer(LATEST).withPassword(PASS);

    @Test
    void checkThatAuthorizationRequired() throws Exception {
        final boolean running = container.isRunning();
        assertTrue(running);

        final URL url = getCheckUrl(container);
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        connection.connect();

        final int status = connection.getResponseCode();
        assertEquals(401, status);
    }
}

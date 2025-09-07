package io.testcontainers.arangodb.containers;

import io.testcontainers.arangodb.ArangoRunner;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
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
class ArangoContainerRandomPortTests extends ArangoRunner {

    @Container
    private static final ArangoContainer<?> container = new ArangoContainer<>(IMAGE_3_7)
            .withoutAuth();

    @Test
    void checkThatDatabaseIsRunning() throws Exception {
        final boolean running = container.isRunning();
        assertTrue(running);

        var uri = getGetCheckURI(container);
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpResponse<String> response = httpClient.send(HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .timeout(Duration.ofSeconds(10))
                .build(), HttpResponse.BodyHandlers.ofString());

        final int status = response.statusCode();
        assertEquals(200, status);
    }
}

package io.testcontainers.arangodb.cluster;

import io.testcontainers.arangodb.ArangoRunner;
import io.testcontainers.arangodb.containers.ArangoContainer;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

/**
 * ArangoDB default cluster configuration tests
 * 
 * @see ArangoClusterDefault
 * @author Anton Kurako (GoodforGod)
 * @since 15.3.2020
 */
@Testcontainers
class ArangoClusterDefaultTests extends ArangoRunner {

    private static ArangoClusterDefault clusterDefault = ArangoClusterDefault.build();

    @Container
    private static final ArangoClusterContainer agent1 = clusterDefault.getAgent1();
    @Container
    private static final ArangoClusterContainer agent2 = clusterDefault.getAgent2();
    @Container
    private static final ArangoClusterContainer agent3 = clusterDefault.getAgent3();
    @Container
    private static final ArangoClusterContainer db1 = clusterDefault.getDatabase1();
    @Container
    private static final ArangoClusterContainer db2 = clusterDefault.getDatabase2();
    @Container
    private static final ArangoClusterContainer coordinator1 = clusterDefault.getCoordinator1();
    @Container
    private static final ArangoClusterContainer coordinator2 = clusterDefault.getCoordinator2();

    @Test
    void allCoordinatorsAreAccessible() throws IOException {
        assertEquals(ArangoClusterContainer.NodeType.AGENT_LEADER, agent1.getType());
        assertEquals(ArangoClusterContainer.NodeType.AGENT, agent2.getType());
        assertEquals(ArangoClusterContainer.NodeType.AGENT, agent3.getType());

        assertTrue(agent1.isRunning());
        assertTrue(agent2.isRunning());
        assertTrue(agent3.isRunning());
        assertTrue(db1.isRunning());
        assertTrue(db2.isRunning());
        assertTrue(coordinator1.isRunning());
        assertTrue(coordinator2.isRunning());

        for (ArangoContainer coordinator : Arrays.asList(coordinator1, coordinator2)) {
            final URL url = getCheckUrl(coordinator);
            final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(35000);
            connection.setReadTimeout(35000);
            connection.connect();

            final int status = connection.getResponseCode();
            final String response = getResponse(connection);

            assertEquals(200, status);
            assertNotNull(response);
            assertFalse(response.isEmpty());
        }
    }
}

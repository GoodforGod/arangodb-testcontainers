package io.testcontainers.arangodb.cluster;

import io.testcontainers.arangodb.ArangoRunner;
import io.testcontainers.arangodb.containers.ArangoContainer;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * ArangoDB default cluster configuration tests
 *
 * @author Anton Kurako (GoodforGod)
 * @since 15.3.2020
 */
@Testcontainers
class ArangoClusterDefaultTests extends ArangoRunner {

    @Container
    private static final ArangoCluster CLUSTER = ArangoClusterBuilder.builder(IMAGE_3_7).build();

    @Test
    void allCoordinatorsAreAccessible() throws IOException {
        final ArangoClusterContainer<?> agent1 = CLUSTER.getAgentLeader();
        final ArangoClusterContainer<?> agent2 = CLUSTER.getAgent(1);
        final ArangoClusterContainer<?> agent3 = CLUSTER.getAgent(2);
        final ArangoClusterContainer<?> db1 = CLUSTER.getDatabase(0);
        final ArangoClusterContainer<?> db2 = CLUSTER.getDatabase(1);
        final ArangoClusterContainer<?> coordinator1 = CLUSTER.getCoordinator(0);
        final ArangoClusterContainer<?> coordinator2 = CLUSTER.getCoordinator(1);

        assertEquals(ArangoClusterContainer.NodeType.AGENT_LEADER, CLUSTER.getAgentLeader().getType());
        assertEquals(ArangoClusterContainer.NodeType.AGENT_LEADER, agent1.getType());
        assertEquals(ArangoClusterContainer.NodeType.AGENT, agent2.getType());
        assertEquals(ArangoClusterContainer.NodeType.AGENT, agent3.getType());

        assertEquals(CLUSTER.getAgentLeader().getType(), agent1.getType());
        assertEquals(CLUSTER.getAgentLeader().getPort(), agent1.getPort());
        assertTrue(agent1.isRunning());
        assertTrue(agent2.isRunning());
        assertTrue(agent3.isRunning());
        assertTrue(db1.isRunning());
        assertTrue(db2.isRunning());
        assertTrue(coordinator1.isRunning());
        assertTrue(coordinator2.isRunning());
        assertEquals(7, CLUSTER.getContainers().size());

        for (ArangoClusterContainer<?> agent : CLUSTER.getAgents()) {
            int port = agent.getPort();
            assertTrue(Arrays.asList(agent1.getPort(), agent2.getPort(), agent3.getPort()).contains(port));
        }

        for (ArangoClusterContainer<?> database : CLUSTER.getDatabases()) {
            int port = database.getPort();
            assertTrue(Arrays.asList(db1.getPort(), db2.getPort()).contains(port));
        }

        for (ArangoClusterContainer<?> coord : CLUSTER.getCoordinators()) {
            int port = coord.getPort();
            assertTrue(Arrays.asList(coordinator1.getPort(), coordinator2.getPort()).contains(port));
        }

        for (ArangoContainer<?> coordinator : Arrays.asList(coordinator1, coordinator2)) {
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

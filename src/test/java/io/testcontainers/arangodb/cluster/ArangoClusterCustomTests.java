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
 * @author Anton Kurako (GoodforGod)
 * @since 15.3.2020
 */
@Testcontainers
class ArangoClusterCustomTests extends ArangoRunner {

    @Container
    private static final ArangoCluster CLUSTER = ArangoCluster.builder(IMAGE_3_11)
            .withAgentNodes(3)
            .withDatabaseNodes(3)
            .withCoordinatorNodes(3)
            .withPassword("jjj")
            .build();

    @Test
    void allCoordinatorsAreAccessible() throws IOException {
        assertEquals(CLUSTER.getCoordinator(0).getHost(), CLUSTER.getHost());
        assertEquals(CLUSTER.getCoordinator(0).getPort(), CLUSTER.getPort());
        assertEquals("root", CLUSTER.getUser());
        assertEquals("jjj", CLUSTER.getPassword());

        for (ArangoCluster.HostAndPort hostsAndPort : CLUSTER.getHostsAndPorts()) {
            assertTrue(CLUSTER.getCoordinators().stream()
                    .anyMatch(c -> hostsAndPort.host().equals(c.getHost()) && hostsAndPort.port() == c.getPort()));
        }

        final ArangoClusterContainer<?> agent1 = CLUSTER.getContainers().get(0);
        final ArangoClusterContainer<?> agent2 = CLUSTER.getContainers().get(1);
        final ArangoClusterContainer<?> agent3 = CLUSTER.getContainers().get(2);

        final ArangoClusterContainer<?> db1 = CLUSTER.getContainers().get(3);
        final ArangoClusterContainer<?> db2 = CLUSTER.getContainers().get(4);
        final ArangoClusterContainer<?> db3 = CLUSTER.getContainers().get(5);

        final ArangoClusterContainer<?> coordinator1 = CLUSTER.getContainers().get(6);
        final ArangoClusterContainer<?> coordinator2 = CLUSTER.getContainers().get(7);
        final ArangoClusterContainer<?> coordinator3 = CLUSTER.getContainers().get(8);

        assertEquals(ArangoClusterContainer.NodeType.AGENT_LEADER, agent1.getType());
        assertEquals(ArangoClusterContainer.NodeType.AGENT, agent2.getType());
        assertEquals(ArangoClusterContainer.NodeType.AGENT, agent3.getType());

        assertTrue(agent1.isRunning());
        assertTrue(agent2.isRunning());
        assertTrue(agent3.isRunning());
        assertTrue(db1.isRunning());
        assertTrue(db2.isRunning());
        assertTrue(db3.isRunning());
        assertTrue(coordinator1.isRunning());
        assertTrue(coordinator2.isRunning());
        assertTrue(coordinator3.isRunning());

        for (ArangoContainer<?> coordinator : Arrays.asList(coordinator1, coordinator2, coordinator3)) {
            final URL url = getCheckUrl(coordinator);
            final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(35000);
            connection.setReadTimeout(35000);
            connection.connect();

            final int status = connection.getResponseCode();
            assertEquals(401, status);
        }
    }
}

package io.testcontainers.arangodb.cluster;

import static io.testcontainers.arangodb.cluster.ArangoClusterBuilder.AGENCY_PORT_DEFAULT;
import static io.testcontainers.arangodb.cluster.ArangoClusterBuilder.DBSERVER_PORT_DEFAULT;

import io.testcontainers.arangodb.ArangoRunner;
import io.testcontainers.arangodb.containers.ArangoContainer;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * @author Anton Kurako (GoodforGod)
 * @since 15.3.2020
 */
@Testcontainers
class ArangoClusterTests extends ArangoRunner {

    private static final ArangoCluster CLUSTER = ArangoClusterBuilder.builder("3.7.13")
            .withCoordinatorNodes(3)
            .withDatabaseNodes(3)
            .build();

    @Container
    private static final ArangoClusterContainer agent1 = CLUSTER.getAgentLeader();
    @Container
    private static final ArangoClusterContainer agent2 = CLUSTER.getAgent(0);
    @Container
    private static final ArangoClusterContainer agent3 = CLUSTER.getAgent(1);

    @Container
    private static final ArangoClusterContainer db1 = CLUSTER.getDatabase(0);
    @Container
    private static final ArangoClusterContainer db2 = CLUSTER.getDatabase(1);
    @Container
    private static final ArangoClusterContainer db3 = CLUSTER.getDatabase(2);

    @Container
    private static final ArangoClusterContainer coordinator1 = CLUSTER.getCoordinator(0);
    @Container
    private static final ArangoClusterContainer coordinator2 = CLUSTER.getCoordinator(1);
    @Container
    private static final ArangoClusterContainer coordinator3 = CLUSTER.getCoordinator(2);

    @Test
    void allCoordinatorsAreAccessible() throws IOException {
        assertEquals(9, CLUSTER.getNodes().size());
        assertEquals(ArangoClusterContainer.NodeType.AGENT_LEADER, CLUSTER.getAgentLeader().getType());
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

        assertNotEquals(AGENCY_PORT_DEFAULT, CLUSTER.getAgentLeaderPort());
        assertFalse(CLUSTER.getAgentPorts().contains(AGENCY_PORT_DEFAULT));
        assertFalse(CLUSTER.getAgentPorts().contains(AGENCY_PORT_DEFAULT + 1));
        assertFalse(CLUSTER.getAgentPorts().contains(AGENCY_PORT_DEFAULT + 2));
        assertFalse(CLUSTER.getDatabasePorts().contains(DBSERVER_PORT_DEFAULT));
        assertFalse(CLUSTER.getDatabasePorts().contains(DBSERVER_PORT_DEFAULT + 1));
        assertFalse(CLUSTER.getDatabasePorts().contains(DBSERVER_PORT_DEFAULT + 2));

        for (ArangoContainer coordinator : CLUSTER.getCoordinators()) {
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

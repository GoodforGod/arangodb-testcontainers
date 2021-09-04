package io.testcontainers.arangodb.cluster;

import static io.testcontainers.arangodb.cluster.ArangoClusterBuilder.AGENCY_PORT_DEFAULT;
import static io.testcontainers.arangodb.cluster.ArangoClusterBuilder.DBSERVER_PORT_DEFAULT;

import io.testcontainers.arangodb.ArangoRunner;
import io.testcontainers.arangodb.containers.ArangoContainer;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * @author Anton Kurako (GoodforGod)
 * @since 15.3.2020
 */
@Testcontainers
class ArangoClusterBuilderExposedTests extends ArangoRunner {

    private static final List<ArangoClusterContainer> CLUSTER = ArangoClusterBuilder.builder("3.7.13")
            .withCoordinatorNodes(3)
            .withDatabaseNodes(3)
            .withExposedAgentNodes()
            .withExposedDBServerNodes()
            .buildContainers();

    @Container
    private static final ArangoClusterContainer agent1 = CLUSTER.get(0);
    @Container
    private static final ArangoClusterContainer agent2 = CLUSTER.get(1);
    @Container
    private static final ArangoClusterContainer agent3 = CLUSTER.get(2);

    @Container
    private static final ArangoClusterContainer db1 = CLUSTER.get(3);
    @Container
    private static final ArangoClusterContainer db2 = CLUSTER.get(4);
    @Container
    private static final ArangoClusterContainer db3 = CLUSTER.get(5);

    @Container
    private static final ArangoClusterContainer coordinator1 = CLUSTER.get(6);
    @Container
    private static final ArangoClusterContainer coordinator2 = CLUSTER.get(7);
    @Container
    private static final ArangoClusterContainer coordinator3 = CLUSTER.get(8);

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
        assertTrue(db3.isRunning());
        assertTrue(coordinator1.isRunning());
        assertTrue(coordinator2.isRunning());
        assertTrue(coordinator3.isRunning());

        assertTrue(agent1.getBoundPortNumbers().contains(AGENCY_PORT_DEFAULT));
        assertTrue(agent2.getBoundPortNumbers().contains(AGENCY_PORT_DEFAULT + 1));
        assertTrue(agent3.getBoundPortNumbers().contains(AGENCY_PORT_DEFAULT + 2));
        assertTrue(db1.getBoundPortNumbers().contains(DBSERVER_PORT_DEFAULT));
        assertTrue(db2.getBoundPortNumbers().contains(DBSERVER_PORT_DEFAULT + 1));
        assertTrue(db3.getBoundPortNumbers().contains(DBSERVER_PORT_DEFAULT + 2));

        for (ArangoContainer coordinator : Arrays.asList(coordinator1, coordinator2, coordinator3)) {
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

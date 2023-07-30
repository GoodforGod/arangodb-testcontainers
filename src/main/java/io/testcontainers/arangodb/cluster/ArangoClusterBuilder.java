package io.testcontainers.arangodb.cluster;

import static io.testcontainers.arangodb.cluster.ArangoClusterContainer.NodeType.*;

import io.testcontainers.arangodb.cluster.ArangoClusterContainer.NodeType;
import io.testcontainers.arangodb.containers.ArangoContainer;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.Nullable;
import org.testcontainers.containers.Network;
import org.testcontainers.utility.DockerImageName;

/**
 * Arango Cluster TestContainer {@link ArangoContainer} Builder.
 * <p>
 * Builds AGENT, DBSERVER, COORDINATOR containers with specified configuration of nodes.
 * <p>
 * <a href="https://www.arangodb.com/docs/stable/deployment-cluster-manual-start.html">Cluster
 * Manual</a>
 *
 * @author Anton Kurako (GoodforGod)
 * @since 15.3.2020
 */
public final class ArangoClusterBuilder {

    private static final int AGENCY_NODES_DEFAULT = 3;
    private static final int DBSERVER_NODES_DEFAULT = 2;
    private static final int COORDINATOR_NODES_DEFAULT = 2;

    private int agentNodes = AGENCY_NODES_DEFAULT;
    private int databaseNodes = DBSERVER_NODES_DEFAULT;
    private int coordinatorNodes = COORDINATOR_NODES_DEFAULT;

    private final DockerImageName image;

    private String password = null;
    private Boolean noAuth = null;

    ArangoClusterBuilder(DockerImageName image) {
        this.image = image;
    }

    /**
     * @param agentNodes amount of {@link NodeType#AGENT} nodes to create in cluster
     * @return self
     */
    public ArangoClusterBuilder withAgentNodes(int agentNodes) {
        this.agentNodes = agentNodes;
        return this;
    }

    /**
     * @param databaseNodes amount of {@link NodeType#DBSERVER} nodes to create in cluster
     * @return self
     */
    public ArangoClusterBuilder withDatabaseNodes(int databaseNodes) {
        this.databaseNodes = databaseNodes;
        return this;
    }

    /**
     * @param coordinatorNodes amount of {@link NodeType#COORDINATOR} nodes to create in cluster
     * @return self
     */
    public ArangoClusterBuilder withCoordinatorNodes(int coordinatorNodes) {
        this.coordinatorNodes = coordinatorNodes;
        return this;
    }

    /**
     * Setup desired JWT for database.
     *
     * @param password to set on startup
     * @return container itself
     */
    public ArangoClusterBuilder withPassword(String password) {
        this.password = password;
        if (noAuth != null) {
            throw new IllegalArgumentException(
                    "Password can't be set when without authentication is enable, please review your configuration");
        }
        return this;
    }

    /**
     * Setup ArangoDB to start without authentication.
     *
     * @return container itself
     */
    public ArangoClusterBuilder withoutAuth() {
        this.noAuth = true;
        if (password != null) {
            throw new IllegalArgumentException(
                    "Without authentication can't be enabled when password is set, please review your configuration");
        }
        return this;
    }

    public ArangoCluster build() {
        return build(null);
    }

    public ArangoCluster build(@Nullable Network network) {
        return new ArangoCluster(buildContainers(network), password);
    }

    private List<ArangoClusterContainer<?>> buildContainers(@Nullable Network network) {
        if (image == null)
            throw new UnsupportedOperationException("Image version can not be empty!");
        if (agentNodes % 2 != 1)
            throw new UnsupportedOperationException("Agent nodes must be odd number!");
        if (databaseNodes < 2)
            throw new IllegalArgumentException("Database nodes can not be less 2");
        if (coordinatorNodes < 2)
            throw new IllegalArgumentException("Coordinator nodes can not be less 2");

        final List<ArangoClusterContainer<?>> agents = new ArrayList<>(agentNodes);
        final List<ArangoClusterContainer<?>> databases = new ArrayList<>(databaseNodes);
        final List<ArangoClusterContainer<?>> coordinators = new ArrayList<>(coordinatorNodes);

        final String aliasLead = AGENT_LEADER.alias();
        final ArangoClusterContainer<?> leader = ArangoClusterContainer.agent(image, aliasLead, agentNodes, true);
        agents.add(leader);

        // Build agencies
        for (int i = 2; i <= agentNodes; i++) {
            final String alias = AGENT.alias(i);
            // Add agency dependency and endpoint of leader agency
            final ArangoClusterContainer<?> agent = ArangoClusterContainer
                    .agent(image, alias, agentNodes, false)
                    .dependsOn(leader);
            agents.add(agent);
        }

        // Build agencies
        for (int i = 1; i <= databaseNodes; i++) {
            final String alias = DBSERVER.alias(i);
            final ArangoClusterContainer<?> database = ArangoClusterContainer.dbserver(image, alias)
                    .dependsOn(agents);
            databases.add(database);
        }

        // Build agencies
        for (int i = 1; i <= coordinatorNodes; i++) {
            final String alias = COORDINATOR.alias(i);
            final ArangoClusterContainer<?> coordinator = ArangoClusterContainer.coordinator(image, alias)
                    .dependsOn(agents);
            coordinators.add(coordinator);
        }

        return Collections.unmodifiableList(Stream.of(agents, databases, coordinators)
                .flatMap(Collection::stream)
                .map(c -> {
                    if (network != null) {
                        c.withNetwork(network);
                    } else {
                        c.withNetwork(Network.SHARED);
                    }

                    if (password != null && c.getType() == COORDINATOR) {
                        c.withPassword(password);
                    } else {
                        c.withoutAuth();
                    }

                    return c;
                })
                .sorted(Comparator.comparing(ArangoClusterContainer::getType))
                .collect(Collectors.toList()));
    }
}

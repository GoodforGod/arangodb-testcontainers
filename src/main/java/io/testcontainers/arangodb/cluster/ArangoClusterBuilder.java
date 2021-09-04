package io.testcontainers.arangodb.cluster;

import static io.testcontainers.arangodb.cluster.ArangoClusterContainer.NodeType.*;

import io.testcontainers.arangodb.cluster.ArangoClusterContainer.NodeType;
import io.testcontainers.arangodb.containers.ArangoContainer;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.testcontainers.containers.Network;
import org.testcontainers.shaded.org.apache.commons.lang.StringUtils;

/**
 * Arango Cluster TestContainer {@link ArangoContainer} Builder. Builds AGENT, DBSERVER, COORDINATOR
 * containers with specified configuration of nodes.
 *
 * https://www.arangodb.com/docs/stable/deployment-cluster-manual-start.html
 *
 * @author Anton Kurako (GoodforGod)
 * @since 15.3.2020
 */
public class ArangoClusterBuilder {

    public static final String LATEST = "latest";

    public static final int COORDINATOR_PORT_DEFAULT = 8529;
    public static final int DBSERVER_PORT_DEFAULT = 8515;
    public static final int AGENCY_PORT_DEFAULT = 8500;

    private static final int AGENCY_NODES_DEFAULT = 3;
    private static final int DBSERVER_NODES_DEFAULT = 2;
    private static final int COORDINATOR_NODES_DEFAULT = 2;

    private int agentNodes = AGENCY_NODES_DEFAULT;
    private int databaseNodes = DBSERVER_NODES_DEFAULT;
    private int coordinatorNodes = COORDINATOR_NODES_DEFAULT;

    private int agentPortFrom = AGENCY_PORT_DEFAULT;
    private int dbserverPortFrom = DBSERVER_PORT_DEFAULT;
    private int coordinatorPortFrom = COORDINATOR_PORT_DEFAULT;

    private String version = ArangoContainer.LATEST;
    private boolean exposeAgentNodes = false;
    private boolean exposeDBServerNodes = false;

    private ArangoClusterBuilder() {}

    /**
     * Exposes {@link NodeType#AGENT} nodes ports as specified per builder configuration
     * 
     * @return self
     */
    public ArangoClusterBuilder withExposedAgentNodes() {
        this.exposeAgentNodes = true;
        return this;
    }

    /**
     * Exposes {@link NodeType#DBSERVER} nodes ports as specified per builder configuration
     * 
     * @return self
     */
    public ArangoClusterBuilder withExposedDBServerNodes() {
        this.exposeDBServerNodes = true;
        return this;
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
     * @param agentPortFrom port to start exposing {@link NodeType#AGENT} nodes
     * @return self
     */
    public ArangoClusterBuilder withAgentPortFrom(int agentPortFrom) {
        this.agentPortFrom = agentPortFrom;
        return this;
    }

    /**
     * @param dbserverPortFrom port to start exposing {@link NodeType#DBSERVER} nodes
     * @return self
     */
    public ArangoClusterBuilder withDBServerPortFrom(int dbserverPortFrom) {
        this.dbserverPortFrom = dbserverPortFrom;
        return this;
    }

    /**
     * @param coordinatorPortFrom port to start exposing {@link NodeType#COORDINATOR} nodes
     * @return self
     */
    public ArangoClusterBuilder withCoordinatorPortFrom(int coordinatorPortFrom) {
        this.coordinatorPortFrom = coordinatorPortFrom;
        return this;
    }

    /**
     * ArangoDB image version
     * 
     * @param version to set for images
     * @return self
     */
    protected ArangoClusterBuilder withVersion(String version) {
        this.version = version;
        return this;
    }

    /**
     * This is recommended usage by TestContainers library
     *
     * @see org.testcontainers.containers.GenericContainer
     * @deprecated use {@link #builder(String)} instead
     * @return builder for cluster
     */
    @Deprecated
    public static ArangoClusterBuilder builder() {
        return builder(ArangoContainer.LATEST);
    }

    public static ArangoClusterBuilder builder(String imageVersion) {
        return new ArangoClusterBuilder().withVersion(imageVersion);
    }

    public static ArangoCluster buildDefault(String imageVersion) {
        return new ArangoClusterBuilder()
                .withVersion(imageVersion)
                .build();
    }

    public static ArangoCluster buildDefault(String imageVersion, int exposeCoordinatorPort) {
        return new ArangoClusterBuilder()
                .withVersion(imageVersion)
                .withCoordinatorPortFrom(exposeCoordinatorPort)
                .build();
    }

    public ArangoCluster build() {
        return build(Network.newNetwork());
    }

    public ArangoCluster build(Network network) {
        return new ArangoCluster(buildContainers(network));
    }

    public List<ArangoClusterContainer> buildContainers() {
        return buildContainers(Network.newNetwork());
    }

    public List<ArangoClusterContainer> buildContainers(Network network) {
        if (StringUtils.isBlank(version))
            throw new UnsupportedOperationException("Image version can not be empty!");
        if (agentNodes % 2 != 1)
            throw new UnsupportedOperationException("Agent nodes must be odd number!");
        if (databaseNodes < 2)
            throw new IllegalArgumentException("Database nodes can not be less 2");
        if (coordinatorNodes < 2)
            throw new IllegalArgumentException("Coordinator nodes can not be less 2");

        final List<ArangoClusterContainer> agents = new ArrayList<>(agentNodes);
        final List<ArangoClusterContainer> databases = new ArrayList<>(databaseNodes);
        final List<ArangoClusterContainer> coordinators = new ArrayList<>(coordinatorNodes);

        ArangoClusterContainer leader = null;

        // Build agencies
        for (int i = 0; i < agentNodes; i++) {
            final String alias = AGENT.getAlias(i);
            final int port = agentPortFrom + i;
            if (i == 0) {
                leader = ArangoClusterContainer.agent(alias, port, version, agentNodes, true, exposeAgentNodes);
                leader.withAgentEndpoints(Collections.singletonList(leader.getEndpoint()));
                agents.add(leader);
            } else {
                // Add agency dependency and endpoint of leader agency
                final ArangoClusterContainer agent = (ArangoClusterContainer) ArangoClusterContainer
                        .agent(alias, port, version, agentNodes, false, exposeAgentNodes)
                        .withAgentEndpoints(Collections.singletonList(leader.getEndpoint()))
                        .dependsOn(leader);
                agents.add(agent);
            }
        }

        // Build agencies
        for (int i = 0; i < databaseNodes; i++) {
            final String alias = DBSERVER.getAlias(i);
            final int port = dbserverPortFrom + i;
            final ArangoContainer database = ArangoClusterContainer.dbserver(alias, port, version, exposeDBServerNodes)
                    .withAgentEndpoints(Collections.singletonList(leader.getEndpoint()))
                    .dependsOn(agents);
            databases.add((ArangoClusterContainer) database);
        }

        // Build agencies
        for (int i = 0; i < coordinatorNodes; i++) {
            final String alias = COORDINATOR.getAlias(i);
            final int port = coordinatorPortFrom + i;
            final ArangoContainer coordinator = ArangoClusterContainer.coordinator(alias, port, version)
                    .withAgentEndpoints(Collections.singletonList(leader.getEndpoint()))
                    .dependsOn(databases);
            coordinators.add((ArangoClusterContainer) coordinator);
        }

        return Stream.of(agents, databases, coordinators)
                .flatMap(Collection::stream)
                .map(c -> ((ArangoClusterContainer) c.withNetwork(network)))
                .sorted(Comparator.comparing(ArangoClusterContainer::getType))
                .collect(Collectors.toList());
    }
}

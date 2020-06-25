package io.testcontainers.arangodb.cluster;

import io.testcontainers.arangodb.cluster.ArangoClusterContainer.NodeType;
import io.testcontainers.arangodb.containers.ArangoContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.lifecycle.Startable;
import org.testcontainers.shaded.org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.testcontainers.arangodb.cluster.ArangoClusterContainer.NodeType.*;
import static java.util.Collections.singletonList;

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

    private int agentNodes = ArangoClusterDefault.AGENCY_NODES_DEFAULT;
    private int databaseNodes = ArangoClusterDefault.DBSERVER_NODES_DEFAULT;
    private int coordinatorNodes = ArangoClusterDefault.COORDINATOR_NODES_DEFAULT;

    private int agentPortFrom = ArangoClusterDefault.AGENCY_PORT_DEFAULT;
    private int dbserverPortFrom = ArangoClusterDefault.DBSERVER_PORT_DEFAULT;
    private int coordinatorPortFrom = ArangoClusterDefault.COORDINATOR_PORT_DEFAULT;

    private String version = ArangoContainer.VERSION_DEFAULT;
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
    public ArangoClusterBuilder withVersion(String version) {
        this.version = version;
        return this;
    }

    public static ArangoClusterBuilder builder() {
        return new ArangoClusterBuilder();
    }

    public List<ArangoClusterContainer> build() {
        return build(Network.newNetwork());
    }

    public List<ArangoClusterContainer> build(Network network) {
        if (agentNodes % 2 != 1)
            throw new UnsupportedOperationException("Agent nodes must be odd number!");

        if (StringUtils.isBlank(version))
            throw new UnsupportedOperationException("Image version can not be empty!");

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
                leader.withAgentEndpoints(singletonList(leader.getEndpoint()));
                agents.add(leader);
            } else {
                // Add agency dependency and endpoint of leader agency
                final ArangoClusterContainer agent = (ArangoClusterContainer) ArangoClusterContainer
                        .agent(alias, port, version, agentNodes, false, exposeAgentNodes)
                        .withAgentEndpoints(singletonList(leader.getEndpoint()))
                        .dependsOn(leader);
                agents.add(agent);
            }
        }

        final List<String> agentEndpoints = agents.stream()
                .map(ArangoClusterContainer::getEndpoint)
                .collect(Collectors.toList());

        // Create database nodes
        final List<Startable> databaseDependsOn = new ArrayList<>(agents);

        // Build agencies
        for (int i = 0; i < databaseNodes; i++) {
            final String alias = DBSERVER.getAlias(i);
            final int port = dbserverPortFrom + i;
            final ArangoContainer database = ArangoClusterContainer.dbserver(alias, port, version, exposeDBServerNodes)
                    .withAgentEndpoints(agentEndpoints)
                    .dependsOn(databaseDependsOn);
            databases.add((ArangoClusterContainer) database);
        }

        // Create coordinators
        final List<Startable> coordinatorDependsOn = new ArrayList<>(databases);

        // Build agencies
        for (int i = 0; i < coordinatorNodes; i++) {
            final String alias = COORDINATOR.getAlias(i);
            final int port = coordinatorPortFrom + i;
            final ArangoContainer coordinator = ArangoClusterContainer.coordinator(alias, port, version)
                    .withAgentEndpoints(agentEndpoints)
                    .dependsOn(coordinatorDependsOn);
            coordinators.add((ArangoClusterContainer) coordinator);
        }

        return Stream.of(agents, databases, coordinators)
                .flatMap(Collection::stream)
                .map(c -> ((ArangoClusterContainer) c.withNetwork(network)))
                .sorted(Comparator.comparing(ArangoClusterContainer::getType))
                .collect(Collectors.toList());
    }
}

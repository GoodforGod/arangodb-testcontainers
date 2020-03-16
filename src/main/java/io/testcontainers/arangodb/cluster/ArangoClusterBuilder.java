package io.testcontainers.arangodb.cluster;

import io.testcontainers.arangodb.cluster.ArangoClusterContainer.NodeType;
import io.testcontainers.arangodb.containers.ArangoContainer;
import org.junit.platform.commons.util.StringUtils;
import org.testcontainers.containers.Network;
import org.testcontainers.lifecycle.Startable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.testcontainers.arangodb.cluster.ArangoClusterContainer.NodeType.*;
import static java.util.Collections.singletonList;

/**
 * Arango Cluster TestContainer {@link ArangoContainer} Builder. Builds agency, dbserver, coordinator
 * containers with specified configuration of nodes.
 *
 * https://www.arangodb.com/docs/stable/deployment-cluster-manual-start.html
 *
 * @author Anton Kurako (GoodforGod)
 * @since 15.3.2020
 */
public class ArangoClusterBuilder {

    private int agencyNodes = ArangoClusterDefault.AGENCY_NODES_DEFAULT;
    private int databaseNodes = ArangoClusterDefault.DBSERVER_NODES_DEFAULT;
    private int coordinatorNodes = ArangoClusterDefault.COORDINATOR_NODES_DEFAULT;

    private int agencyPortFrom = ArangoClusterDefault.AGENCY_PORT_DEFAULT;
    private int dbserverPortFrom = ArangoClusterDefault.DBSERVER_PORT_DEFAULT;
    private int coordinatorPortFrom = ArangoClusterDefault.COORDINATOR_PORT_DEFAULT;

    private String version = ArangoContainer.VERSION_DEFAULT;
    private boolean exposeAgencyNodes = false;
    private boolean exposeDBServerNodes = false;

    private ArangoClusterBuilder() {}

    /**
     * Exposes {@link NodeType#AGENCY} nodes ports as specified per builder configuration
     * 
     * @return self
     */
    public ArangoClusterBuilder withExposedAgencyNodes() {
        this.exposeAgencyNodes = true;
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
     * @param agencyNodes amount of {@link NodeType#AGENCY} nodes to create in cluster
     * @return self
     */
    public ArangoClusterBuilder withAgencyNodes(int agencyNodes) {
        this.agencyNodes = agencyNodes;
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
     * @param agencyPortFrom port to start exposing {@link NodeType#AGENCY} nodes
     * @return self
     */
    public ArangoClusterBuilder withAgencyPortFrom(int agencyPortFrom) {
        this.agencyPortFrom = agencyPortFrom;
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
        if (agencyNodes % 2 != 1)
            throw new UnsupportedOperationException("Agency nodes must be odd number!");

        if (StringUtils.isBlank(version))
            throw new UnsupportedOperationException("Image version can not be empty!");

        final List<ArangoClusterContainer> agencies = new ArrayList<>(agencyNodes);
        final List<ArangoClusterContainer> databases = new ArrayList<>(databaseNodes);
        final List<ArangoClusterContainer> coordinators = new ArrayList<>(coordinatorNodes);

        ArangoClusterContainer leader = null;

        // Build agencies
        for (int i = 0; i < agencyNodes; i++) {
            final String alias = AGENCY.getAlias(i);
            final int port = agencyPortFrom + i;
            if (i == 0) {
                leader = ArangoClusterContainer.agency(alias, port, version, agencyNodes, true, exposeAgencyNodes);
                leader.withAgencyEndpoints(singletonList(leader.getEndpoint()));
                agencies.add(leader);
            } else {
                // Add agency dependency and endpoint of leader agency
                final ArangoClusterContainer agency = (ArangoClusterContainer) ArangoClusterContainer
                        .agency(alias, port, version, agencyNodes, false, exposeAgencyNodes)
                        .withAgencyEndpoints(singletonList(leader.getEndpoint()))
                        .dependsOn(leader);
                agencies.add(agency);
            }
        }

        final List<String> agencyEndpoints = agencies.stream()
                .map(ArangoClusterContainer::getEndpoint)
                .collect(Collectors.toList());

        // Create database nodes
        final List<Startable> databaseDependsOn = new ArrayList<>(agencies);

        // Build agencies
        for (int i = 0; i < databaseNodes; i++) {
            final String alias = DBSERVER.getAlias(i);
            final int port = dbserverPortFrom + i;
            final ArangoContainer database = ArangoClusterContainer.dbserver(alias, port, version, exposeDBServerNodes)
                    .withAgencyEndpoints(agencyEndpoints)
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
                    .withAgencyEndpoints(agencyEndpoints)
                    .dependsOn(coordinatorDependsOn);
            coordinators.add((ArangoClusterContainer) coordinator);
        }

        return Stream.of(agencies, databases, coordinators)
                .flatMap(Collection::stream)
                .map(c -> ((ArangoClusterContainer) c.withNetwork(network)))
                .sorted(Comparator.comparing(ArangoClusterContainer::getType))
                .collect(Collectors.toList());
    }
}

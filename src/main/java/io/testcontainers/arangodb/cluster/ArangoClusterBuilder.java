package io.testcontainers.arangodb.cluster;

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
 * Description in progress
 *
 * @author Anton Kurako (GoodforGod)
 * @since 15.3.2020
 */
public class ArangoClusterBuilder {

    private int agencyNodes = ArangoClusterDefault.AGENCY_NODES_DEFAULT;
    private int databaseNodes = ArangoClusterDefault.DATABASE_NODES_DEFAULT;
    private int coordinatorNodes = ArangoClusterDefault.COORDINATOR_NODES_DEFAULT;

    private int agencyPortFrom = ArangoClusterDefault.AGENCY_PORT_DEFAULT;
    private int databasePortFrom = ArangoClusterDefault.DATABASE_PORT_DEFAULT;
    private int coordinatorPortFrom = ArangoClusterDefault.COORDINATOR_PORT_DEFAULT;

    private String version = ArangoContainer.VERSION_DEFAULT;

    private ArangoClusterBuilder() {
    }

    public ArangoClusterBuilder withAgencyNodes(int agencyNodes) {
        this.agencyNodes = agencyNodes;
        return this;
    }

    public ArangoClusterBuilder withDatabaseNodes(int databaseNodes) {
        this.databaseNodes = databaseNodes;
        return this;
    }

    public ArangoClusterBuilder withCoordinatorNodes(int coordinatorNodes) {
        this.coordinatorNodes = coordinatorNodes;
        return this;
    }

    public ArangoClusterBuilder withAgencyPortFrom(int agencyPortFrom) {
        this.agencyPortFrom = agencyPortFrom;
        return this;
    }

    public ArangoClusterBuilder withDatabasePortFrom(int databasePortFrom) {
        this.databasePortFrom = databasePortFrom;
        return this;
    }

    public ArangoClusterBuilder withCoordinatorPortFrom(int coordinatorPortFrom) {
        this.coordinatorPortFrom = coordinatorPortFrom;
        return this;
    }

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
                leader = ArangoClusterContainer.agency(alias, port, version, agencyNodes,
                        true);
                leader.withAgencyEndpoints(singletonList(leader.getEndpoint()));
                agencies.add(leader);
            } else {
                // Add agency dependency and endpoint of leader agency
                final ArangoClusterContainer agency = (ArangoClusterContainer) ArangoClusterContainer
                        .agency(alias, port, version, agencyNodes, false)
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
            final int port = databasePortFrom + i;
            final ArangoContainer database = ArangoClusterContainer.dbserver(alias, port, version)
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

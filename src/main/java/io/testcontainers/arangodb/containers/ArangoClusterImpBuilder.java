package io.testcontainers.arangodb.containers;

import com.github.dockerjava.api.exception.NotFoundException;
import org.testcontainers.containers.Network;
import org.testcontainers.lifecycle.Startable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.testcontainers.arangodb.containers.ArangoClusterContainer.NodeType.*;
import static java.util.Collections.emptyList;

/**
 * Description in progress
 *
 * @author Anton Kurako (GoodforGod)
 * @since 15.3.2020
 */
public class ArangoClusterImpBuilder {

    private int agencyNodes = ArangoClusterDefault.AGENCY_NODES_DEFAULT;
    private int databaseNodes = ArangoClusterDefault.DATABASE_NODES_DEFAULT;
    private int coordinatorNodes = ArangoClusterDefault.COORDINATOR_NODES_DEFAULT;

    private int agencyPortFrom = ArangoClusterDefault.AGENCY_PORT_DEFAULT;
    private int databasePortFrom = ArangoClusterDefault.DATABASE_PORT_DEFAULT;
    private int coordinatorPortFrom = ArangoClusterDefault.COORDINATOR_PORT_DEFAULT;

    private ArangoClusterImpBuilder() {
    }

    public static ArangoClusterImpBuilder builder() {
        return new ArangoClusterImpBuilder();
    }

    public ArangoClusterImpBuilder setAgencyNodes(int agencyNodes) {
        this.agencyNodes = agencyNodes;
        return this;
    }

    public ArangoClusterImpBuilder setDatabaseNodes(int databaseNodes) {
        this.databaseNodes = databaseNodes;
        return this;
    }

    public ArangoClusterImpBuilder setCoordinatorNodes(int coordinatorNodes) {
        this.coordinatorNodes = coordinatorNodes;
        return this;
    }

    public ArangoClusterImpBuilder setAgencyPortFrom(int agencyPortFrom) {
        this.agencyPortFrom = agencyPortFrom;
        return this;
    }

    public ArangoClusterImpBuilder setDatabasePortFrom(int databasePortFrom) {
        this.databasePortFrom = databasePortFrom;
        return this;
    }

    public ArangoClusterImpBuilder setCoordinatorPortFrom(int coordinatorPortFrom) {
        this.coordinatorPortFrom = coordinatorPortFrom;
        return this;
    }

    public List<ArangoClusterContainer> build() {
        return build(Network.newNetwork());
    }

    public List<ArangoClusterContainer> build(Network network) {
        final List<ArangoClusterContainer> agencies = new ArrayList<>(agencyNodes);
        final List<ArangoClusterContainer> databases = new ArrayList<>(databaseNodes);
        final List<ArangoClusterContainer> coordinators = new ArrayList<>(coordinatorNodes);

        // Build agencies
        for (int i = 0; i < agencyNodes; i++) {
            final String alias = AGENCY.getAlias(i);
            final int port = agencyPortFrom + i;
            if (agencyNodes - 1 == i) {
                agencies.add(ArangoClusterContainer.agency(true, alias, port, emptyList()));
            } else {
                agencies.add(ArangoClusterContainer.agency(alias, port, emptyList()));
            }
        }

        final List<String> agencyEndpoints = agencies.stream()
                .map(ArangoClusterContainer::getNodeAddress)
                .collect(Collectors.toList());

        // Add agencies to the one to coordinate and discovery for cluster
        final ArangoClusterContainer centralAgency = agencies.stream()
                .filter(a -> a.getType().equals(AGENCY_LEADER))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Arango Agency Leader Not Found!"));

        final List<Startable> centralDependsOn = agencies.stream()
                .filter(a -> !a.getNodeAddress().equals(centralAgency.getNodeAddress()))
                .collect(Collectors.toList());
        centralAgency.addAgencies(false, agencyEndpoints).dependsOn(centralDependsOn);

        // Create database nodes
        final List<Startable> databaseDependsOn = new ArrayList<>(agencies);

        // Build agencies
        for (int i = 0; i < databaseNodes; i++) {
            final String alias = DATABASE.getAlias(i);
            final int port = databasePortFrom + i;
            final ArangoContainer database = ArangoClusterContainer.database(alias, port, agencyEndpoints).dependsOn(databaseDependsOn);
            databases.add((ArangoClusterContainer) database);
        }

        // Create coordinators
        final List<Startable> coordinatorDependsOn = new ArrayList<>(databases);

        // Build agencies
        for (int i = 0; i < coordinatorNodes; i++) {
            final String alias = COORDINATOR.getAlias(i);
            final int port = coordinatorPortFrom + i;
            final ArangoContainer coordinator = ArangoClusterContainer.coordinator(alias, port, agencyEndpoints).dependsOn(coordinatorDependsOn);
            coordinators.add((ArangoClusterContainer) coordinator);
        }

        return Stream.of(agencies, databases, coordinators)
                .flatMap(Collection::stream)
                .map(c -> ((ArangoClusterContainer) c.withNetwork(network)))
                .collect(Collectors.toList());
    }
}

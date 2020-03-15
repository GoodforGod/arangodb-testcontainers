package io.testcontainers.arangodb.containers;

import com.github.dockerjava.api.exception.NotFoundException;
import org.junit.platform.commons.util.StringUtils;
import org.testcontainers.containers.Network;
import org.testcontainers.lifecycle.Startable;

import java.util.*;
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

    private String version = ArangoContainer.LATEST_VERSION;

    private ArangoClusterImpBuilder() { }

    public ArangoClusterImpBuilder withAgencyNodes(int agencyNodes) {
        this.agencyNodes = agencyNodes;
        return this;
    }

    public ArangoClusterImpBuilder withDatabaseNodes(int databaseNodes) {
        this.databaseNodes = databaseNodes;
        return this;
    }

    public ArangoClusterImpBuilder withCoordinatorNodes(int coordinatorNodes) {
        this.coordinatorNodes = coordinatorNodes;
        return this;
    }

    public ArangoClusterImpBuilder  withAgencyPortFrom(int agencyPortFrom) {
        this.agencyPortFrom = agencyPortFrom;
        return this;
    }

    public ArangoClusterImpBuilder  withDatabasePortFrom(int databasePortFrom) {
        this.databasePortFrom = databasePortFrom;
        return this;
    }

    public ArangoClusterImpBuilder  withCoordinatorPortFrom(int coordinatorPortFrom) {
        this.coordinatorPortFrom = coordinatorPortFrom;
        return this;
    }

    public ArangoClusterImpBuilder withVersion(String version) {
        this.version = version;
        return this;
    }

    public static ArangoClusterImpBuilder builder() {
        return new ArangoClusterImpBuilder();
    }

    public List<ArangoClusterContainer> build() {
        return build(Network.newNetwork());
    }

    public List<ArangoClusterContainer> build(Network network) {
        if(agencyNodes % 2 != 1)
            throw new UnsupportedOperationException("Agency nodes must be odd number!");

        if(StringUtils.isBlank(version))
            throw new UnsupportedOperationException("Image version can not be empty!");

        final List<ArangoClusterContainer> agencies = new ArrayList<>(agencyNodes);
        final List<ArangoClusterContainer> databases = new ArrayList<>(databaseNodes);
        final List<ArangoClusterContainer> coordinators = new ArrayList<>(coordinatorNodes);

        Collection<String> leaderEndpoint = emptyList();

        // Build agencies
        for (int i = 0; i < agencyNodes; i++) {
            final String alias = AGENCY.getAlias(i);
            final int port = agencyPortFrom + i;
            if (i == 0) {
                final ArangoClusterContainer leader = ArangoClusterContainer.agency(alias, port, version, agencyNodes, true);
                leaderEndpoint = Collections.singletonList(leader.getEndpoint());
                leader.withAgencies(leaderEndpoint);
                agencies.add(leader);
            } else {
                final ArangoClusterContainer agency = ArangoClusterContainer.agency(alias, port, version, agencyNodes, false)
                        .withAgencies(leaderEndpoint);
                agencies.add(agency);
            }
        }

        // Add agencies to the one to coordinate and discovery for cluster
        final ArangoClusterContainer leadAgency = agencies.stream()
                .filter(a -> a.getType().equals(AGENCY_LEADER))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Arango Agency Leader Not Found!"));

        // Depend agencies on lead agency
        agencies.stream()
                .filter(a -> !a.getType().equals(AGENCY_LEADER))
                .forEach(c -> c.dependsOn(leadAgency));

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
                    .withAgenciesCluster(agencyEndpoints)
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
                    .withAgenciesCluster(agencyEndpoints)
                    .dependsOn(coordinatorDependsOn);
            coordinators.add((ArangoClusterContainer) coordinator);
        }

        return Stream.of(agencies, databases, coordinators)
                .flatMap(Collection::stream)
                .map(c -> ((ArangoClusterContainer) c.withNetwork(network)))
                .collect(Collectors.toList());
    }
}

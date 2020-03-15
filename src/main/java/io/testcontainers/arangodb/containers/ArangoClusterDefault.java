package io.testcontainers.arangodb.containers;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Arango small cluster impl
 *
 * @author Anton Kurako (GoodforGod)
 * @see ArangoClusterImpBuilder
 * @since 15.3.2020
 */
public class ArangoClusterDefault {

    public static final int AGENCY_NODES_DEFAULT = 3;
    public static final int DATABASE_NODES_DEFAULT = 2;
    public static final int COORDINATOR_NODES_DEFAULT = 2;

    public static final int AGENCY_PORT_DEFAULT = 8500;
    public static final int DATABASE_PORT_DEFAULT = 8515;
    public static final int COORDINATOR_PORT_DEFAULT = 8530;

    /**
     * Containts default number of nodes
     */
    private final List<ArangoClusterContainer> containers;

    public static ArangoClusterDefault build() {
        return new ArangoClusterDefault(ArangoClusterImpBuilder.builder().build());
    }

    public static ArangoClusterDefault build(String version) {
        return new ArangoClusterDefault(ArangoClusterImpBuilder.builder()
                .withVersion(version)
                .build());
    }

    private ArangoClusterDefault(Collection<ArangoClusterContainer> containers) {
        this.containers = containers.stream()
                .sorted(Comparator.comparing(ArangoClusterContainer::getType))
                .collect(Collectors.toList());

        if(this.containers.get(0).getType().equals(ArangoClusterContainer.NodeType.AGENCY_LEADER))
            Collections.swap(this.containers, 0, 2);
        if(this.containers.get(1).getType().equals(ArangoClusterContainer.NodeType.AGENCY_LEADER))
            Collections.swap(this.containers, 1, 2);
    }

    public List<ArangoClusterContainer> getContainers() {
        return new ArrayList<>(containers);
    }

    public ArangoClusterContainer getAgency1() {
        return containers.get(0);
    }

    public ArangoClusterContainer getAgency2() {
        return containers.get(1);
    }

    public ArangoClusterContainer getAgency3() {
        return containers.get(2);
    }

    public ArangoClusterContainer getDatabase1() {
        return containers.get(3);
    }

    public ArangoClusterContainer getDatabase2() {
        return containers.get(4);
    }

    public ArangoClusterContainer getCoordinator1() {
        return containers.get(5);
    }

    public ArangoClusterContainer getCoordinator2() {
        return containers.get(6);
    }
}

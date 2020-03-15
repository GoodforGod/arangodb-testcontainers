package io.testcontainers.arangodb.containers;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Arango small cluster impl
 *
 * @author Anton Kurako (GoodforGod)
 * @see ArangoClusterImpBuilder
 * @since 15.3.2020
 */
public class ArangoClusterDefault {

    /**
     * Containts default number of nodes
     */
    private final List<ArangoClusterContainer> containers;

    public static ArangoClusterDefault get() {
        return new ArangoClusterDefault(new ArangoClusterImpBuilder().build());
    }

    private ArangoClusterDefault(Collection<ArangoClusterContainer> containers) {
        this.containers = containers.stream()
                .sorted(Comparator.comparing(ArangoClusterContainer::getType))
                .collect(Collectors.toList());
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

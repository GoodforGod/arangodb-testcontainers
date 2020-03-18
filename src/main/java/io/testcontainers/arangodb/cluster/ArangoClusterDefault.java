package io.testcontainers.arangodb.cluster;

import io.testcontainers.arangodb.containers.ArangoContainer;

import java.util.*;

/**
 * ArangoDB TestContainer small cluster configuration. 3 Agent nodes, 2 DBServer nodes, 2 Coordinator nodes.
 * Cluster is available on 8529 port.
 *
 * @author Anton Kurako (GoodforGod)
 * @see ArangoClusterBuilder
 * @since 15.3.2020
 */
public class ArangoClusterDefault {

    public static final int AGENCY_NODES_DEFAULT = 3;
    public static final int DBSERVER_NODES_DEFAULT = 2;
    public static final int COORDINATOR_NODES_DEFAULT = 2;

    /**
     * Cluster default port.
     */
    public static final int COORDINATOR_PORT_DEFAULT = 8529;
    public static final int DBSERVER_PORT_DEFAULT = 8515;
    public static final int AGENCY_PORT_DEFAULT = 8500;

    /**
     * Containts default number of nodes
     */
    private final List<ArangoClusterContainer> containers;

    public static ArangoClusterDefault build() {
        return build(COORDINATOR_PORT_DEFAULT);
    }

    /**
     * @see ArangoClusterBuilder#withCoordinatorPortFrom(int)
     * @param coordinatorPortFrom port to start exposing coordinators
     * @return self
     */
    public static ArangoClusterDefault build(int coordinatorPortFrom) {
        return build(COORDINATOR_PORT_DEFAULT, ArangoContainer.VERSION_DEFAULT);
    }

    /**
     * ArangoDB image version
     * 
     * @param version for iamges
     * @return self
     */
    public static ArangoClusterDefault build(String version) {
        return build(COORDINATOR_PORT_DEFAULT, version);
    }

    /**
     * Specify coordinator port where cluster will be available.
     * 
     * @param coordinatorPortFrom to start mapping coordinators from.
     * @param version             ArangoDB version
     * @return self
     */
    public static ArangoClusterDefault build(int coordinatorPortFrom, String version) {
        return new ArangoClusterDefault(ArangoClusterBuilder.builder()
                .withCoordinatorPortFrom(coordinatorPortFrom)
                .withVersion(version)
                .build());
    }

    private ArangoClusterDefault(List<ArangoClusterContainer> containers) {
        this.containers = containers;

        if (this.containers.get(1).getType().equals(ArangoClusterContainer.NodeType.AGENT_LEADER))
            Collections.swap(this.containers, 1, 0);
        if (this.containers.get(2).getType().equals(ArangoClusterContainer.NodeType.AGENT_LEADER))
            Collections.swap(this.containers, 2, 0);
    }

    public List<ArangoClusterContainer> getContainers() {
        return new ArrayList<>(containers);
    }

    public ArangoClusterContainer getAgent1() {
        return containers.get(0);
    }

    public ArangoClusterContainer getAgent2() {
        return containers.get(1);
    }

    public ArangoClusterContainer getAgent3() {
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

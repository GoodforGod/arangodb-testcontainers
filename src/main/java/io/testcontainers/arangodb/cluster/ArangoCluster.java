package io.testcontainers.arangodb.cluster;

import io.testcontainers.arangodb.cluster.ArangoClusterContainer.NodeType;
import io.testcontainers.arangodb.containers.ArangoContainer;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Anton Kurako (GoodforGod)
 * @since 14.11.2020
 */
public class ArangoCluster {

    private final List<ArangoClusterContainer> coordinators;
    private final List<ArangoClusterContainer> agents;
    private final List<ArangoClusterContainer> databases;

    public ArangoCluster(List<ArangoClusterContainer> containers) {
        this.coordinators = containers.stream()
                .filter(c -> c.getType().equals(NodeType.COORDINATOR))
                .collect(Collectors.toList());

        this.agents = containers.stream()
                .filter(c -> c.getType().equals(NodeType.AGENT) || c.getType().equals(NodeType.AGENT_LEADER))
                .collect(Collectors.toList());

        this.databases = containers.stream()
                .filter(c -> c.getType().equals(NodeType.DBSERVER))
                .collect(Collectors.toList());
    }

    public List<ArangoClusterContainer> getNodes() {
        return Stream.of(getAgents(), getCoordinators(), getDatabases())
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public List<ArangoClusterContainer> getCoordinators() {
        return coordinators;
    }

    public ArangoClusterContainer getCoordinator(int i) {
        return getCoordinators().get(i);
    }

    public ArangoClusterContainer getAgentLeader() {
        return agents.stream()
                .filter(c -> c.getType().equals(NodeType.AGENT_LEADER))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Agent leader not found!"));
    }

    public int getAgentLeaderPort() {
        return getAgentLeader().getPort();
    }

    public List<ArangoClusterContainer> getAgents() {
        return agents;
    }

    public ArangoClusterContainer getAgent(int i) {
        return getAgents().get(i);
    }

    public List<ArangoClusterContainer> getDatabases() {
        return databases;
    }

    public ArangoClusterContainer getDatabase(int i) {
        return getDatabases().get(i);
    }

    public List<Integer> getCoordinatorPorts() {
        return getCoordinators().stream()
                .map(ArangoContainer::getPort)
                .collect(Collectors.toList());
    }

    public List<Integer> getAgentPorts() {
        return getCoordinators().stream()
                .map(ArangoContainer::getPort)
                .collect(Collectors.toList());
    }

    public List<Integer> getDatabasePorts() {
        return getCoordinators().stream()
                .map(ArangoContainer::getPort)
                .collect(Collectors.toList());
    }
}

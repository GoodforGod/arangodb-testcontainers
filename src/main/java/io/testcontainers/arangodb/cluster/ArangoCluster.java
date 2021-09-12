package io.testcontainers.arangodb.cluster;

import io.testcontainers.arangodb.cluster.ArangoClusterContainer.NodeType;
import io.testcontainers.arangodb.containers.ArangoContainer;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Anton Kurako (GoodforGod)
 * @since 14.11.2020
 */
public class ArangoCluster {

    private final List<ArangoClusterContainer> coordinators;
    private final ArangoClusterContainer agentLeader;
    private final List<ArangoClusterContainer> agents;
    private final List<ArangoClusterContainer> databases;

    public ArangoCluster(List<ArangoClusterContainer> containers) {
        this.coordinators = Collections.unmodifiableList(containers.stream()
                .filter(c -> c.getType().equals(NodeType.COORDINATOR))
                .collect(Collectors.toList()));

        this.agents = Collections.unmodifiableList(containers.stream()
                .filter(c -> c.getType().equals(NodeType.AGENT))
                .collect(Collectors.toList()));

        this.agentLeader = containers.stream()
                .filter(c -> c.getType().equals(NodeType.AGENT_LEADER))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Agent leader is not present!"));

        this.databases = Collections.unmodifiableList(containers.stream()
                .filter(c -> c.getType().equals(NodeType.DBSERVER))
                .collect(Collectors.toList()));
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

    public List<ArangoClusterContainer> getAgents() {
        return Stream.of(Collections.singletonList(agentLeader), agents)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
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
        return getAgents().stream()
                .map(ArangoContainer::getPort)
                .collect(Collectors.toList());
    }

    public List<Integer> getDatabasePorts() {
        return getDatabases().stream()
                .map(ArangoContainer::getPort)
                .collect(Collectors.toList());
    }

    public ArangoClusterContainer getAgentLeader() {
        return this.agentLeader;
    }

    public int getAgentLeaderPort() {
        return getAgentLeader().getPort();
    }

    public ArangoClusterContainer getAgent1() {
        return getAgentLeader();
    }

    public ArangoClusterContainer getAgent2() {
        return agents.get(0);
    }

    public ArangoClusterContainer getAgent3() {
        return agents.get(1);
    }

    public ArangoClusterContainer getDatabase1() {
        return databases.get(0);
    }

    public ArangoClusterContainer getDatabase2() {
        return databases.get(1);
    }

    public ArangoClusterContainer getCoordinator1() {
        return coordinators.get(0);
    }

    public ArangoClusterContainer getCoordinator2() {
        return coordinators.get(1);
    }
}

package io.testcontainers.arangodb.cluster;

import io.testcontainers.arangodb.cluster.ArangoClusterContainer.NodeType;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.testcontainers.lifecycle.Startable;

/**
 * @author Anton Kurako (GoodforGod)
 * @since 14.11.2020
 */
public final class ArangoCluster implements Startable {

    private static final String DEFAULT_USER = "root";

    private final ArangoClusterContainer<?> agentLeader;
    private final List<ArangoClusterContainer<?>> coordinators;
    private final List<ArangoClusterContainer<?>> agents;
    private final List<ArangoClusterContainer<?>> databases;

    private final String password;

    ArangoCluster(List<ArangoClusterContainer<?>> containers, String password) {
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
        this.password = password;
    }

    public List<ArangoClusterContainer<?>> getContainers() {
        return Stream.of(getAgents(), getDatabases(), getCoordinators())
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public List<ArangoClusterContainer<?>> getCoordinators() {
        return coordinators;
    }

    public ArangoClusterContainer<?> getCoordinator(int i) {
        return getCoordinators().get(i);
    }

    public List<ArangoClusterContainer<?>> getAgents() {
        return Stream.of(Collections.singletonList(agentLeader), agents)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public ArangoClusterContainer<?> getAgent(int i) {
        return getAgents().get(i);
    }

    public List<ArangoClusterContainer<?>> getDatabases() {
        return databases;
    }

    public ArangoClusterContainer<?> getDatabase(int i) {
        return getDatabases().get(i);
    }

    public ArangoClusterContainer<?> getAgentLeader() {
        return this.agentLeader;
    }

    public String getPassword() {
        return password;
    }

    public String getUser() {
        return DEFAULT_USER;
    }

    public String getHost() {
        return getCoordinator(0).getHost();
    }

    public Integer getPort() {
        return getCoordinator(0).getPort();
    }

    @Override
    public void start() {
        try {
            agentLeader.start();

            final CompletableFuture[] agentFutures = agents.stream()
                    .map(c -> CompletableFuture.runAsync(c::start))
                    .toArray(CompletableFuture[]::new);
            CompletableFuture.allOf(agentFutures).get(15, TimeUnit.MINUTES);

            final CompletableFuture[] otherFutures = Stream.concat(databases.stream(), coordinators.stream())
                    .map(c -> CompletableFuture.runAsync(c::start))
                    .toArray(CompletableFuture[]::new);
            CompletableFuture.allOf(otherFutures).get(15, TimeUnit.MINUTES);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void stop() {
        try {
            final CompletableFuture[] coordinatorsFutures = coordinators.stream()
                    .map(c -> CompletableFuture.runAsync(c::stop))
                    .toArray(CompletableFuture[]::new);
            CompletableFuture.allOf(coordinatorsFutures).get(15, TimeUnit.MINUTES);

            final CompletableFuture[] dbFutures = databases.stream()
                    .map(c -> CompletableFuture.runAsync(c::stop))
                    .toArray(CompletableFuture[]::new);
            CompletableFuture.allOf(dbFutures).get(15, TimeUnit.MINUTES);

            final CompletableFuture[] agentFutures = agents.stream()
                    .map(c -> CompletableFuture.runAsync(c::stop))
                    .toArray(CompletableFuture[]::new);
            CompletableFuture.allOf(agentFutures).get(15, TimeUnit.MINUTES);

            agentLeader.stop();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}

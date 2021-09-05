package io.testcontainers.arangodb.cluster;

import io.testcontainers.arangodb.containers.ArangoContainer;
import java.util.*;
import java.util.function.Consumer;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.containers.output.Slf4jLogConsumer;

/**
 * ArangoDB Cluster TestContainer implementation.
 *
 * @author Anton Kurako (GoodforGod)
 * @see ArangoContainer
 * @since 15.3.2020
 */
public class ArangoClusterContainer extends ArangoContainer {

    public enum NodeType {

        AGENT_LEADER("agentLeader"),
        AGENT("agent"),
        DBSERVER("dbserver"),
        COORDINATOR("coordinator");

        private final String alias;

        NodeType(String alias) {
            this.alias = alias;
        }

        public String getAlias() {
            return alias;
        }

        public String getAlias(int number) {
            return (this.equals(AGENT_LEADER))
                    ? alias
                    : alias + number;
        }
    }

    private String alias;
    private String endpoint;
    private NodeType type;

    protected ArangoClusterContainer(String version) {
        super(version);
    }

    @Override
    protected Consumer<OutputFrame> getOutputConsumer() {
        return new Slf4jLogConsumer(LoggerFactory.getLogger(getClass().getName() + " [" + type + "]"));
    }

    protected ArangoClusterContainer withAgentEndpoints(Collection<String> agentEndpoints) {
        final String prefix = NodeType.AGENT.equals(type) || NodeType.AGENT_LEADER.equals(type)
                ? "--agency.endpoint"
                : "--cluster.agency-endpoint";

        final List<String> cmd = new ArrayList<>(Arrays.asList(this.getCommandParts()));
        agentEndpoints.forEach(agentEndpoint -> {
            cmd.add(prefix);
            cmd.add(agentEndpoint);
        });

        this.setCommand(cmd.toArray(new String[0]));
        return this;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public NodeType getType() {
        return type;
    }

    protected static ArangoClusterContainer agent(String alias, int port, String version, int totalAgentNodes,
                                                  boolean leader, boolean expose) {
        final String endpoint = getEndpoint(alias);
        final List<String> cmd = getCommonCommand(alias);
        cmd.add("--agency.my-address " + endpoint);
        cmd.add("--agency.activate true");
        cmd.add("--agency.size " + totalAgentNodes);
        cmd.add("--agency.supervision true");

        final ArangoClusterContainer container = build(version, cmd, alias, port, expose);
        container.type = (leader) ? NodeType.AGENT_LEADER : NodeType.AGENT;
        container.endpoint = endpoint;
        container.alias = alias;
        container.setNetworkAliases(Collections.singletonList(alias));
        return container;
    }

    protected static ArangoClusterContainer dbserver(String alias, int port, String version, boolean expose) {
        final String endpoint = getEndpoint(alias);
        final List<String> cmd = getCommonCommand(alias);
        cmd.add("--cluster.my-local-info " + alias);
        cmd.add("--cluster.my-role DBSERVER");
        cmd.add("--cluster.my-address " + endpoint);

        final ArangoClusterContainer container = build(version, cmd, alias, port, expose);
        container.endpoint = endpoint;
        container.type = NodeType.DBSERVER;
        container.alias = alias;
        container.setNetworkAliases(Collections.singletonList(alias));
        return container;
    }

    protected static ArangoClusterContainer coordinator(String alias, int port, String version) {
        final String endpoint = getEndpoint(alias);
        final List<String> cmd = getCommonCommand(alias);
        cmd.add("--cluster.my-local-info " + alias);
        cmd.add("--cluster.my-role COORDINATOR");
        cmd.add("--cluster.my-address " + endpoint);

        final ArangoClusterContainer container = build(version, cmd, alias, port, true);
        container.endpoint = endpoint;
        container.type = NodeType.COORDINATOR;
        container.alias = alias;
        container.setNetworkAliases(Collections.singletonList(alias));
        return container;
    }

    private static List<String> getCommonCommand(String alias) {
        final List<String> cmd = new ArrayList<>();
        cmd.add("arangod");
        cmd.add("--server.authentication=false");
        cmd.add("--server.endpoint " + "tcp://0.0.0.0:" + ArangoContainer.DEFAULT_PORT);
        return cmd;
    }

    private static String getEndpoint(String alias) {
        return "tcp://" + alias + ":" + ArangoContainer.DEFAULT_PORT;
    }

    private static ArangoClusterContainer build(String version,
                                                List<String> commandArguments,
                                                String networkAliasName,
                                                int port,
                                                boolean expose) {
        final String cmd = commandArguments.stream()
                .reduce((s1, s2) -> s1 + " " + s2)
                .orElseThrow(() -> new IllegalArgumentException("No Args"));

        final ArangoClusterContainer container = (ArangoClusterContainer) new ArangoClusterContainer(version)
                .withoutAuth()
                .withNetworkAliases(networkAliasName)
                .withCommand(cmd);

        return (expose)
                ? (ArangoClusterContainer) container.withFixedPort(port)
                : container;
    }

    @Override
    public String getContainerName() {
        return alias;
    }
}

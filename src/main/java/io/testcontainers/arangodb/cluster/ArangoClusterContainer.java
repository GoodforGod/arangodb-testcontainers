package io.testcontainers.arangodb.cluster;

import io.testcontainers.arangodb.containers.ArangoContainer;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.containers.output.Slf4jLogConsumer;

import java.util.Collection;
import java.util.StringJoiner;
import java.util.function.Consumer;

/**
 * ArangoDB Cluster TestContainer implementation.
 *
 * @author Anton Kurako (GoodforGod)
 * @see ArangoContainer
 * @since 15.3.2020
 */
public class ArangoClusterContainer extends ArangoContainer {

    public enum NodeType {

        AGENT_LEADER("agent"),
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
            return alias + number;
        }
    }

    private String endpoint;
    private NodeType type;

    protected ArangoClusterContainer() {
        super();
    }

    protected ArangoClusterContainer(String version) {
        super(version);
    }

    @Override
    protected Consumer<OutputFrame> getOutputConsumer() {
        return new Slf4jLogConsumer(LoggerFactory.getLogger(getClass().getName() + " (" + type + ")"));
    }

    protected ArangoClusterContainer withAgentEndpoints(Collection<String> agentEndpoints) {
        final String prefix = (NodeType.AGENT.equals(type) || NodeType.AGENT_LEADER.equals(type))
                ? "--agency.endpoint"
                : "--cluster.agency-endpoint";

        final StringJoiner cmd = new StringJoiner(" ");
        for (String commandPart : getCommandParts())
            cmd.add(commandPart);

        agentEndpoints.forEach(endpoint -> cmd.add(prefix).add(endpoint));
        return (ArangoClusterContainer) this.withCommand(cmd.toString());
    }

    public String getEndpoint() {
        return endpoint;
    }

    public NodeType getType() {
        return type;
    }

    protected static ArangoClusterContainer agent(String alias, int port, String version, int totalAgentNodes,
                                                  boolean leader, boolean expose) {
        final StringJoiner cmd = new StringJoiner(" ");
        final String endpoint = "tcp://" + alias + ":" + ArangoContainer.PORT_DEFAULT;
        cmd.add("arangod")
                .add("--server.authentication=false")
                .add("--server.endpoint").add("tcp://0.0.0.0:" + ArangoContainer.PORT_DEFAULT)
                .add("--agency.my-address").add(endpoint)
                .add("--agency.activate true")
                .add("--agency.size").add(String.valueOf(totalAgentNodes))
                .add("--agency.supervision true")
                .add("--database.directory").add(alias);

        final ArangoClusterContainer container = build(version, cmd.toString(), alias, port, expose);
        container.type = (leader) ? NodeType.AGENT_LEADER : NodeType.AGENT;
        container.endpoint = endpoint;
        return container;
    }

    protected static ArangoClusterContainer dbserver(String alias, int port, String version, boolean expose) {
        final StringJoiner cmd = new StringJoiner(" ");
        final String endpoint = "tcp://" + alias + ":" + ArangoContainer.PORT_DEFAULT;
        cmd.add("arangod")
                .add("--server.authentication=false")
                .add("--server.endpoint").add("tcp://0.0.0.0:" + ArangoContainer.PORT_DEFAULT)
                .add("--cluster.my-address").add(endpoint)
                .add("--cluster.my-role DBSERVER")
                .add("--database.directory").add(alias);

        final ArangoClusterContainer container = build(version, cmd.toString(), alias, port, expose);
        container.endpoint = endpoint;
        container.type = NodeType.DBSERVER;
        return container;
    }

    protected static ArangoClusterContainer coordinator(String alias, int port, String version) {
        final StringJoiner cmd = new StringJoiner(" ");
        final String endpoint = "tcp://" + alias + ":" + ArangoContainer.PORT_DEFAULT;
        cmd.add("arangod")
                .add("--server.authentication=false")
                .add("--server.endpoint").add("tcp://0.0.0.0:" + ArangoContainer.PORT_DEFAULT)
                .add("--cluster.my-address").add(endpoint)
                .add("--cluster.my-role COORDINATOR")
                .add("--database.directory").add(alias);

        final ArangoClusterContainer container = build(version, cmd.toString(), alias, port, true);
        container.endpoint = endpoint;
        container.type = NodeType.COORDINATOR;
        return container;
    }

    private static ArangoClusterContainer build(String version, String cmd, String networkAliasName, int port, boolean expose) {
        final ArangoClusterContainer container = (ArangoClusterContainer) new ArangoClusterContainer(version)
                .withPort(port)
                .withoutAuth()
                .withNetworkAliases(networkAliasName)
                .withCommand(cmd);

        return (expose)
                ? container
                : (ArangoClusterContainer) container.withRandomPort();
    }
}

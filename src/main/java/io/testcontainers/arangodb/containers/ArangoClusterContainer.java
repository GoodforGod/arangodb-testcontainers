package io.testcontainers.arangodb.containers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.StringJoiner;

/**
 * ArangoDB Cluster TestContainer implementation.
 *
 * @author Anton Kurako (GoodforGod)
 * @see ArangoContainer
 * @since 15.3.2020
 */
public class ArangoClusterContainer extends ArangoContainer {

    public enum NodeType {
        AGENCY("agency"),
        AGENCY_LEADER("agency"),
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
    protected Logger getLogger() {
        return LoggerFactory.getLogger(getClass().getName() + " (" + type + ")");
    }

    protected ArangoClusterContainer withAgenciesCluster(Collection<String> agenciesEndpoints) {
        return addAgencyEndpoints("--cluster.agency-endpoint", agenciesEndpoints);
    }

    protected ArangoClusterContainer withAgencies(Collection<String> agenciesEndpoints) {
        return addAgencyEndpoints("--agency.endpoint", agenciesEndpoints);
    }

    private ArangoClusterContainer addAgencyEndpoints(String prefix, Collection<String> agenciesEndpoints) {
        final StringJoiner joiner = new StringJoiner(" ");
        for (String commandPart : getCommandParts())
            joiner.add(commandPart);

        agenciesEndpoints.forEach(endpoint -> joiner.add(prefix).add(endpoint));
        return (ArangoClusterContainer) self()
                .withCommand(joiner.toString());
    }

    public String getEndpoint() {
        return endpoint;
    }

    public NodeType getType() {
        return type;
    }

    protected static ArangoClusterContainer agency(String alias, int port, String version, int totalAgencyNodes, boolean leader) {
        final StringJoiner joiner = new StringJoiner(" ");
        final String endpoint = "tcp://" + alias + ":" + port;
        joiner.add("arangod")
                .add("--server.authentication=false")
                .add("--server.endpoint").add("tcp://0.0.0.0:" + port)
                .add("--agency.my-address").add(endpoint)
                .add("--agency.activate true")
                .add("--agency.size").add(String.valueOf(totalAgencyNodes))
                .add("--agency.supervision true")
                .add("--database.directory").add(alias);

        final ArangoClusterContainer container = build(version, joiner.toString(), alias, port);
        container.type = (leader) ? NodeType.AGENCY_LEADER : NodeType.AGENCY;
        container.endpoint = endpoint;
        return container;
    }

    protected static ArangoClusterContainer dbserver(String alias, int port, String version) {
        final StringJoiner joiner = new StringJoiner(" ");
        final String endpoint = "tcp://" + alias + ":" + port;
        joiner.add("arangod")
                .add("--server.authentication=false")
                .add("--server.endpoint").add("tcp://0.0.0.0:" + port)
                .add("--cluster.my-address").add(endpoint)
                .add("--cluster.my-local-info").add(alias)
                .add("--cluster.my-role DBSERVER")
                .add("--database.directory").add(alias);

        final ArangoClusterContainer container = build(version, joiner.toString(), alias, port);
        container.endpoint = endpoint;
        container.type = NodeType.DBSERVER;
        return container;
    }

    protected static ArangoClusterContainer coordinator(String alias, int port, String version) {
        final StringJoiner joiner = new StringJoiner(" ");
        final String endpoint = "tcp://" + alias + ":" + port;
        joiner.add("arangod")
                .add("--server.authentication=false")
                .add("--server.endpoint").add("tcp://0.0.0.0:" + port)
                .add("--cluster.my-address").add(endpoint)
                .add("--cluster.my-local-info").add(alias)
                .add("--cluster.my-role COORDINATOR")
                .add("--database.directory").add(alias);

        final ArangoClusterContainer container = build(version, joiner.toString(), alias, port);
        container.endpoint = endpoint;
        container.type = NodeType.COORDINATOR;
        return container;
    }

    private static ArangoClusterContainer build(String version, String cmd, String networkAliasName, int port) {
        return (ArangoClusterContainer) new ArangoClusterContainer(version)
                .withoutAuth()
                .withPort(null)
                .withExposedPorts(port)
                .withNetworkAliases(networkAliasName)
                .withCommand(cmd);
    }
}



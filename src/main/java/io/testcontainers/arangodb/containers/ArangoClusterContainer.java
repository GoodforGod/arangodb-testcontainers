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
        DATABASE("database"),
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

    private String nodeAddress;
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

    protected ArangoClusterContainer addAgencies(boolean isCluster, Collection<String> agenciesEndpoints) {
        final String prefix = (isCluster) ? "--cluster.agency-endpoint" : "--agency.endpoint";
        final StringJoiner joiner = new StringJoiner(" ");
        for (String commandPart : getCommandParts())
            joiner.add(commandPart);

        agenciesEndpoints.forEach(endpoint -> joiner.add(prefix).add(endpoint));
        return (ArangoClusterContainer) self()
                .withCommand(joiner.toString());
    }

    public String getNodeAddress() {
        return nodeAddress;
    }

    public NodeType getType() {
        return type;
    }

    protected static ArangoClusterContainer agency(String alias, int port, Collection<String> agencyEndpoints) {
        return agency(false, alias, port, agencyEndpoints);
    }

    protected static ArangoClusterContainer agency(boolean leader, String alias, int port, Collection<String> agencyEndpoints) {
        final StringJoiner joiner = new StringJoiner(" ");
        final String myAddress = "tcp://" + alias + ":" + port;
        joiner.add("arangod")
                .add("--server.authentication=false")
                .add("--server.endpoint").add("tcp://0.0.0.0:" + port)
                .add("--agency.my-address").add(myAddress)
                .add("--agency.activate true")
                .add("--agency.size 3")
                .add("--agency.supervision true")
                .add("--database.directory").add(alias);

        final ArangoClusterContainer container = build(joiner.toString(), alias, port)
                .addAgencies(false, agencyEndpoints);

        container.type = (leader) ? NodeType.AGENCY_LEADER : NodeType.AGENCY;
        container.nodeAddress = myAddress;
        return container;
    }

    protected static ArangoClusterContainer database(String alias, int port, Collection<String> agenciesEndpoints) {
        final StringJoiner joiner = new StringJoiner(" ");
        final String myAddress = "tcp://" + alias + ":" + port;
        joiner.add("arangod")
                .add("--server.authentication=false")
                .add("--server.endpoint").add("tcp://0.0.0.0:" + port)
                .add("--cluster.my-address").add(myAddress)
                .add("--cluster.my-local-info").add(alias)
                .add("--cluster.my-role PRIMARY")
                .add("--database.directory").add(alias);

        final ArangoClusterContainer container = build(joiner.toString(), alias, port)
                .addAgencies(true, agenciesEndpoints);

        container.nodeAddress = myAddress;
        container.type = NodeType.DATABASE;
        return container;
    }

    protected static ArangoClusterContainer coordinator(String alias, int port, Collection<String> agenciesEndpoints) {
        final StringJoiner joiner = new StringJoiner(" ");
        final String myAddress = "tcp://" + alias + ":" + port;
        joiner.add("arangod")
                .add("--server.authentication=false")
                .add("--server.endpoint").add("tcp://0.0.0.0:" + port)
                .add("--cluster.my-address").add(myAddress)
                .add("--cluster.my-local-info").add(alias)
                .add("--cluster.my-role COORDINATOR")
                .add("--database.directory").add(alias);

        final ArangoClusterContainer container = build(joiner.toString(), alias, port)
                .addAgencies(true, agenciesEndpoints);

        container.nodeAddress = myAddress;
        container.type = NodeType.COORDINATOR;
        return container;
    }

    private static ArangoClusterContainer build(String cmd, String networkAliasName, int port) {
        return (ArangoClusterContainer) new ArangoClusterContainer()
                .withoutAuth()
                .withPort(port).withInternalPort(port)
                .withExposedPorts(port)
                .withNetworkAliases(networkAliasName)
                .withCommand(cmd);
    }
}



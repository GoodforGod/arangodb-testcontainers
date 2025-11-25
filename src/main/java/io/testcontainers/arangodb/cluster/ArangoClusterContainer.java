package io.testcontainers.arangodb.cluster;

import io.testcontainers.arangodb.containers.ArangoContainer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.utility.DockerImageName;

/**
 * ArangoDB Cluster TestContainer implementation.
 *
 * @author Anton Kurako (GoodforGod)
 * @see ArangoClusterBuilder
 * @since 15.3.2020
 */
public class ArangoClusterContainer extends ArangoContainer {

    public enum NodeType {

        AGENT_LEADER("agent-leader"),
        AGENT("agent"),
        DBSERVER("dbserver"),
        COORDINATOR("coordinator");

        private final String alias;

        NodeType(String alias) {
            this.alias = alias;
        }

        public String alias(String clusterId) {
            return alias + "-" + clusterId;
        }

        public String alias(String clusterId, int number) {
            return (this.equals(AGENT_LEADER))
                    ? alias + "-" + clusterId
                    : alias + "-" + clusterId + "-" + number;
        }
    }

    private final NodeType type;
    private final String alias;

    private ArangoClusterContainer(DockerImageName dockerImageName, NodeType type, String alias) {
        super(dockerImageName);
        this.type = type;
        this.alias = alias;
        withLogConsumer(new Slf4jLogConsumer(
                LoggerFactory.getLogger(ArangoClusterContainer.class.getCanonicalName() + " [" + alias + "]")));
    }

    public NodeType getType() {
        return type;
    }

    static ArangoClusterContainer agent(DockerImageName image,
                                        String clusterId,
                                        int nodeNumber,
                                        int totalAgentNodes,
                                        boolean leader) {
        final String aliasLeader = NodeType.AGENT_LEADER.alias(clusterId);
        final String alias = (leader)
                ? aliasLeader
                : NodeType.AGENT.alias(clusterId, nodeNumber);

        final String endpoint = getEndpoint(alias);
        final List<String> cmd = getCommonCommand(alias);
        cmd.add("--agency.my-address");
        cmd.add(endpoint);
        cmd.add("--agency.activate");
        cmd.add("true");
        cmd.add("--agency.size");
        cmd.add(String.valueOf(totalAgentNodes));
        cmd.add("--agency.supervision");
        cmd.add("true");
        cmd.add("--database.directory");
        cmd.add("agent");
        cmd.add("--agency.endpoint");
        cmd.add(getEndpoint(aliasLeader));

        final NodeType type = (leader)
                ? NodeType.AGENT_LEADER
                : NodeType.AGENT;
        return (ArangoClusterContainer) new ArangoClusterContainer(image, type, alias)
                .withNetworkAliases(alias)
                .withCommand(cmd.toArray(new String[0]));
    }

    static ArangoClusterContainer dbserver(DockerImageName image,
                                           String clusterId,
                                           int nodeNumber) {
        final String alias = NodeType.DBSERVER.alias(clusterId, nodeNumber);
        final String aliasLeader = NodeType.AGENT_LEADER.alias(clusterId);

        final String endpoint = getEndpoint(alias);
        final List<String> cmd = getCommonCommand(alias);
        cmd.add("--cluster.my-local-info");
        cmd.add(alias);
        cmd.add("--cluster.my-role");
        cmd.add("DBSERVER");
        cmd.add("--cluster.my-address");
        cmd.add(endpoint);
        cmd.add("--database.directory");
        cmd.add("dbserver");
        cmd.add("--cluster.agency-endpoint");
        cmd.add(getEndpoint(aliasLeader));

        return (ArangoClusterContainer) new ArangoClusterContainer(image, NodeType.DBSERVER, alias)
                .withNetworkAliases(alias)
                .withCommand(cmd.toArray(new String[0]));
    }

    static ArangoClusterContainer coordinator(DockerImageName image,
                                              String clusterId,
                                              int nodeNumber) {
        final String alias = NodeType.COORDINATOR.alias(clusterId, nodeNumber);
        final String aliasLeader = NodeType.AGENT_LEADER.alias(clusterId);

        final String endpoint = getEndpoint(alias);
        final List<String> cmd = getCommonCommand(alias);
        cmd.add("--cluster.my-local-info");
        cmd.add(alias);
        cmd.add("--cluster.my-role");
        cmd.add("COORDINATOR");
        cmd.add("--cluster.my-address");
        cmd.add(endpoint);
        cmd.add("--database.directory");
        cmd.add("coordinator");
        cmd.add("--cluster.agency-endpoint");
        cmd.add(getEndpoint(aliasLeader));

        return (ArangoClusterContainer) new ArangoClusterContainer(image, NodeType.COORDINATOR, alias)
                .withNetworkAliases(alias)
                .withCommand(cmd.toArray(new String[0]));
    }

    private static List<String> getCommonCommand(String alias) {
        final List<String> cmd = new ArrayList<>();
        cmd.add("arangod");
        cmd.add("--server.endpoint");
        cmd.add("tcp://0.0.0.0:" + ArangoContainer.PORT);
        return cmd;
    }

    private static String getEndpoint(String alias) {
        return "tcp://" + alias + ":" + ArangoContainer.PORT;
    }

    @Override
    public ArangoClusterContainer withoutAuth() {
        final List<String> cmd = new ArrayList<>(Arrays.asList(this.getCommandParts()));
        cmd.add("--server.authentication=false");
        this.setCommand(cmd.toArray(new String[0]));

        return (ArangoClusterContainer) super.withoutAuth();
    }

    @Override
    public ArangoClusterContainer withPassword(String password) {
        return (ArangoClusterContainer) super.withPassword(password);
    }

    @Override
    public ArangoClusterContainer withRandomPassword() {
        return (ArangoClusterContainer) super.withRandomPassword();
    }

    @Override
    public String getPassword() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getContainerName() {
        return super.getContainerName() + "[" + alias + "]";
    }
}

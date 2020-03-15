package io.testcontainers.arangodb.containers;

import org.testcontainers.containers.Network;

import java.util.Arrays;
import java.util.List;

/**
 * Description in progress
 *
 * @author Anton Kurako (GoodforGod)
 * @since 15.3.2020
 */
public class ArangoClusterBuilder {

    @SuppressWarnings("rawtypes")
    public static List<ArangoContainer> getCluster() {
        final Network network = Network.newNetwork();

        final String agency1Alias = "agency1";
        final String agency2Alias = "agency2";
        final String agency3Alias = "agency3";
        final String coordinator1Alias = "coordinator1";
        final String coordinator2Alias = "coordinator2";
        final String db1Alias = "db1";
        final String db2Alias = "db2";

        final String s1 = "arangod " +
                "--server.authentication false " +
                "--server.endpoint tcp://0.0.0.0:5001 " +
                "--agency.my-address=tcp://" + agency1Alias + ":5001 " +
                "--agency.activate true " +
                "--agency.size 3 " +
//                "--agency.endpoint tcp://" + agency1Alias + ":5001 " +
                "--agency.supervision true " +
                "--database.directory " + agency1Alias;

        final String s2 = "arangod " +
                "--server.authentication false " +
                "--server.endpoint tcp://0.0.0.0:5002 " +
                "--agency.my-address=tcp://" + agency2Alias + ":5002 " +
                "--agency.activate true " +
                "--agency.size 3 " +
//                "--agency.endpoint tcp://" + agency1Alias + ":5001 " +
                "--agency.supervision true " +
                "--database.directory " + agency2Alias;

        final String s3 = "arangod " +
                "--server.authentication false " +
                "--server.endpoint tcp://0.0.0.0:5003 " +
                "--agency.my-address=tcp://" + agency3Alias + ":5003 " +
                "--agency.activate true " +
                "--agency.size 3 " +
                "--agency.endpoint tcp://" + agency1Alias + ":5001 " +
                "--agency.endpoint tcp://" + agency2Alias + ":5002 " +
                "--agency.endpoint tcp://" + agency3Alias + ":5003 " +
                "--agency.supervision true " +
                "--database.directory " + agency3Alias;

        final ArangoContainer agency1 = new ArangoContainer()
                .withoutAuth()
                .withoutStartAwait()
                .withPort(null)
                .withExposedPorts(5001)
                .withNetwork(network)
                .withNetworkAliases(agency1Alias)
                .withCommand(s1);

        final ArangoContainer agency2 = new ArangoContainer()
                .withoutAuth()
                .withoutStartAwait()
                .withPort(null)
                .withExposedPorts(5002)
                .withNetwork(network)
                .withNetworkAliases(agency2Alias)
                .withCommand(s2);

        final ArangoContainer agency3 = new ArangoContainer()
                .withoutAuth()
                .withoutStartAwait()
                .withPort(null)
                .withExposedPorts(5003)
                .withNetwork(network)
                .withNetworkAliases(agency3Alias)
                .dependsOn(agency1, agency2)
                .withCommand(s3);

        final String d1 = "arangod " +
                "--server.authentication=false " +
                "--server.endpoint tcp://0.0.0.0:6001 " +
                "--cluster.my-address tcp://" + db1Alias + ":6001 " +
                "--cluster.my-local-info " + db1Alias + " " +
                "--cluster.my-role PRIMARY " +
                "--cluster.agency-endpoint tcp://" + agency1Alias + ":5001 " +
                "--cluster.agency-endpoint tcp://" + agency2Alias + ":5002 " +
                "--cluster.agency-endpoint tcp://" + agency3Alias + ":5003 " +
                "--database.directory " + db1Alias;

        final String d2 = "arangod " +
                "--server.authentication=false " +
                "--server.endpoint tcp://0.0.0.0:6002 " +
                "--cluster.my-address tcp://" + db2Alias + ":6002 " +
                "--cluster.my-local-info " + db2Alias + " " +
                "--cluster.my-role PRIMARY " +
                "--cluster.agency-endpoint tcp://" + agency1Alias + ":5001 " +
                "--cluster.agency-endpoint tcp://" + agency2Alias + ":5002 " +
                "--cluster.agency-endpoint tcp://" + agency3Alias + ":5003 " +
                "--database.directory " + db2Alias;

        final ArangoContainer db1 = new ArangoContainer()
                .withoutAuth()
                .withoutStartAwait()
                .withPort(null)
                .withExposedPorts(6001)
                .dependsOn(agency1, agency2, agency3)
                .withNetwork(network)
                .withNetworkAliases(db1Alias)
                .withCommand(d1);

        final ArangoContainer db2 = new ArangoContainer()
                .withoutAuth()
                .withoutStartAwait()
                .withPort(null)
                .withExposedPorts(6002)
                .dependsOn(agency1, agency2, agency3)
                .withNetwork(network)
                .withNetworkAliases(db2Alias)
                .withCommand(d2);

        final String c1 = "arangod " +
                "--server.authentication=false " +
                "--server.endpoint tcp://0.0.0.0:7001 " +
                "--cluster.my-address tcp://" + coordinator1Alias + ":7001 " +
                "--cluster.my-local-info " + coordinator1Alias + " " +
                "--cluster.my-role COORDINATOR " +
                "--cluster.agency-endpoint tcp://" + agency1Alias + ":5001 " +
                "--cluster.agency-endpoint tcp://" + agency2Alias + ":5002 " +
                "--cluster.agency-endpoint tcp://" + agency3Alias + ":5003 " +
                "--database.directory " + coordinator1Alias;

        final String c2 = "arangod " +
                "--server.authentication=false " +
                "--server.endpoint tcp://0.0.0.0:7002 " +
                "--cluster.my-address tcp://" + coordinator2Alias + ":7002 " +
                "--cluster.my-local-info " + coordinator2Alias + " " +
                "--cluster.my-role COORDINATOR " +
                "--cluster.agency-endpoint tcp://" + agency1Alias + ":5001 " +
                "--cluster.agency-endpoint tcp://" + agency2Alias + ":5002 " +
                "--cluster.agency-endpoint tcp://" + agency3Alias + ":5003 " +
                "--database.directory " + coordinator2Alias;

        final ArangoContainer coordinator1 = new ArangoContainer()
                .withoutAuth()
                .withoutStartAwait()
                .withPort(null)
                .withExposedPorts(7001)
                .dependsOn(db1, db2)
                .withNetwork(network)
                .withNetworkAliases(coordinator1Alias)
                .withCommand(c1);

        final ArangoContainer coordinator2 = new ArangoContainer()
                .withoutAuth()
                .withoutStartAwait()
                .withPort(null)
                .withExposedPorts(7002)
                .dependsOn(db1, db2)
                .withNetwork(network)
                .withNetworkAliases(coordinator2Alias)
                .withCommand(c2);

        return Arrays.asList(agency1, agency2, agency3, db1, db2, coordinator1, coordinator2);
    }
}


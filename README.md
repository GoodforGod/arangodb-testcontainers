# ArangoDB (Cluster) TestContainers

![Java CI](https://github.com/GoodforGod/arangodb-testcontainer/workflows/Java%20CI/badge.svg)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=GoodforGod_arangodb-testcontainer&metric=alert_status)](https://sonarcloud.io/dashboard?id=GoodforGod_arangodb-testcontainer)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=GoodforGod_arangodb-testcontainer&metric=coverage)](https://sonarcloud.io/dashboard?id=GoodforGod_arangodb-testcontainer)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=GoodforGod_arangodb-testcontainer&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=GoodforGod_arangodb-testcontainer)

This is [*ArangoDB*](https://www.arangodb.com/) [TestContainers](https://www.testcontainers.org/) implementation with proper start up strategy and even ArangoDB Cluster test containers' setup.

## Dependency :rocket:
**Gradle**
```groovy
dependencies {
    compile 'com.github.goodforgod:arangodb-testcontainer:1.3.0'
}
```

**Maven**
```xml
<dependency>
    <groupId>com.github.goodforgod</groupId>
    <artifactId>arangodb-testcontainer</artifactId>
    <version>1.3.0</version>
</dependency>
```

## Usage

Check [this](https://www.testcontainers.org/test_framework_integration/junit_5/) TestContainers tutorials for **Jupiter / JUnit 5** examples
or [this](https://www.testcontainers.org/quickstart/junit_4_quickstart/) TestContainers tutorials for **JUnit 4** examples.

Run ArangoDB container *without* authentication.
```java
@Testcontainers
class ArangoContainerTests {

    @Container
    private static final ArangoContainer container = new ArangoContainer().withoutAuth();

    @Test
    void checkContainerIsRunning() {
        assertTrue(container.isRunning());
    }
}
```

Run ArangoDB Cluster *without* authentication (no auth option is available in cluster mode).
Check [this section](#Cluster) for more info.

```java
@Testcontainers
class ArangoContainerTests {

    private static ArangoClusterDefault cluster = ArangoClusterDefault.build();
    
    @Container
    private static final ArangoClusterContainer agent1 = cluster.getAgent1();
    @Container
    private static final ArangoClusterContainer agent2 = cluster.getAgent2();
    @Container
    private static final ArangoClusterContainer agent3 = cluster.getAgent3();
    @Container
    private static final ArangoClusterContainer db1 = cluster.getDatabase1();
    @Container
    private static final ArangoClusterContainer db2 = cluster.getDatabase2();
    @Container
    private static final ArangoClusterContainer coordinator1 = cluster.getCoordinator1();
    @Container
    private static final ArangoClusterContainer coordinator2 = cluster.getCoordinator2();

    @Test
    void checkContainerIsRunning() {
        assertTrue(coordinator1.isRunning());
    }
}
```

## Container

### Up & Running

Container implements *startup strategy* and will be *available to TestContainer framework automatically* when database will be ready for accepting connections.

Check [here](https://www.testcontainers.org/features/startup_and_waits/) for more info about strategies.

### Port

Container runs by default on *random* port, you can explicitly set fixed port via **withFixedPort()** contract.

You can specify desired port with specified setter or choose default ArangoDB port as fixed one via *ArangoContainer.PORT_DEFAULT*.

```java
@Testcontainers
class ArangoContainerTests {

    @Container
    private static final ArangoContainer container = new ArangoContainer().withoutAuth().withFixedPort(5689);

    @Test
    void checkContainerIsRunning() {
        assertTrue(container.isRunning());
    }
```

### Auth

All authentication options are available as per [ArangoDB Docker description](https://hub.docker.com/_/arangodb).

*Without authentication or password or random password* configuration is **required** as per [docker image]().

#### Without Authentication

You can run ArangoDB without authentication by specifying with setter.

```java
@Testcontainers
class ArangoContainerTests {

    @Container
    private static final ArangoContainer container = new ArangoContainer().withoutAuth();

    @Test
    void checkContainerIsRunning() {
        assertTrue(container.isRunning());
    }
}
```

#### With Password

Database default user is *root*. You can specify desired password that will be assigned to *root* user.

```java
@Testcontainers
class ArangoContainerTests {

    @Container
    private static final ArangoContainer container = new ArangoContainer().withPassword("mypass");

    @Test
    void checkContainerIsRunning() {
        assertTrue(container.isRunning());
    }
}
```

#### With Random Password

You can run container with random password for root user, 
but is such case, there is no methods to retrieve that password.
You will have to retrieve it somehow by your own.

```java
@Testcontainers
class ArangoContainerTests {

    @Container
    private static final ArangoContainer container = new ArangoContainer().withRandomPassword();

    @Test
    void checkContainerIsRunning() {
        assertTrue(container.isRunning());
    }
}
```

## Cluster

You can run ArangoDB cluster as TestContainers.

Default cluster with 3 Agent nodes, 2 DBServer nodes and 2 Coordinator nodes is preconfigured for easy usage.

Arango Cluster Builder is available to build custom cluster configuration.

### Default Cluster

You can run containers with all accessible TestContainer configurations.
Just build default cluster via *ArangoClusterDefault* builder.

Keep order of your containers as per example below, *FIRST start agent nodes, SECOND database servers, THIRD coordinators*.

All containers have dependency on other *containers must be run in correct order* for cluster to initialize.

**If you have problems with cluster initializations, check container run order**.

```java
@Testcontainers
class ArangoContainerTests {

    private static ArangoClusterDefault cluster = ArangoClusterDefault.build();
    
    @Container
    private static final ArangoClusterContainer agent1 = cluster.getAgent1();
    @Container
    private static final ArangoClusterContainer agent2 = cluster.getAgent2();
    @Container
    private static final ArangoClusterContainer agent3 = cluster.getAgent3();
    @Container
    private static final ArangoClusterContainer db1 = cluster.getDatabase1();
    @Container
    private static final ArangoClusterContainer db2 = cluster.getDatabase2();
    @Container
    private static final ArangoClusterContainer coordinator1 = cluster.getCoordinator1();
    @Container
    private static final ArangoClusterContainer coordinator2 = cluster.getCoordinator2();

    @Test
    void checkContainerIsRunning() {
        
    }
}
```

Cluster is available on **default 8529 port** as default, you can change port in builder. 
```java
ArangoClusterDefault.build(int coordinatorPortFromversion)
```

### Cluster Builder

You can build cluster with desired size via *ArangoClusterBuilder*.

Returns list of containers sorted in order they must be run.
You can check each container type via specified cluster container method.

```java
final List<ArangoClusterContainer> clusterNodes = ArangoClusterBuilder.builder()
            .withCoordinatorNodes(3)        // 3 coordinator nodes in cluster
            .withDatabaseNodes(3)           // 3 dbserver nodes in cluster
            .withExposedAgentNodes()        // expose agent nodes (not exposed by default)
            .withExposedDBServerNodes()     // exposes dbserver nodes (not exposed by default)
            .build();
```

## Versions

**1.3.0** - By default container runs on random port, withFixedPort() contract instead of just withPort().

**1.2.0** - TestContainers Jupiter dependency hidden from exposure (add separately), random port mapping option, other minor improvements.

**1.1.0** - Arango Cluster Containers, Arango Cluster Builder, improved ArangoContainer.

**1.0.0** - Initial project with auth, port set, startup strategy.

## License

This project licensed under the MIT - see the [LICENSE](LICENSE) file for details.


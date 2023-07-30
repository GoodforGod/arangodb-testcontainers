# ArangoDB TestContainers

[![Minimum required Java version](https://img.shields.io/badge/Java-8%2B-blue?logo=openjdk)](https://openjdk.org/projects/jdk8/)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.goodforgod/arangodb-testcontainer/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.goodforgod/arangodb-testcontainer)
![Java CI](https://github.com/GoodforGod/arangodb-testcontainer/workflows/Java%20CI/badge.svg)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=GoodforGod_arangodb-testcontainer&metric=alert_status)](https://sonarcloud.io/dashboard?id=GoodforGod_arangodb-testcontainer)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=GoodforGod_arangodb-testcontainer&metric=coverage)](https://sonarcloud.io/dashboard?id=GoodforGod_arangodb-testcontainer)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=GoodforGod_arangodb-testcontainer&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=GoodforGod_arangodb-testcontainer)

This is [ArangoDB TestContainers](https://testcontainers.com/modules/arangodb/) module for running database as Docker container.

Features:
- ArangoDB [container](#container).
- ArangoDB [cluster](#cluster).

## Dependency :rocket:

**Gradle**
```groovy
testImplementation "com.github.goodforgod:arangodb-testcontainer:3.0.0"
```

**Maven**
```xml
<dependency>
    <groupId>com.github.goodforgod</groupId>
    <artifactId>arangodb-testcontainer</artifactId>
    <version>3.0.0</version>
    <scope>test</scope>
</dependency>
```

## Usage

Check [this](https://www.testcontainers.org/test_framework_integration/junit_5/) TestContainers tutorials for **Jupiter / JUnit 5** examples.

Run ArangoDB container *without* authentication.
```java
@Testcontainers
class ArangoContainerTests {

    @Container
    private static final ArangoContainer<?> container = new ArangoContainer<>("arangodb:3.11.2")
            .withoutAuth();

    @Test
    void checkContainerIsRunning() {
        assertTrue(container.isRunning());
    }
}
```

Run ArangoDB Cluster *without* authentication.

```java
@Testcontainers
class ArangoContainerTests {

    @Container
    private static final ArangoCluster CLUSTER = ArangoCluster.builder("arangodb:3.11.2")
            .withoutAuth()
            .build();

    @Test
    void checkContainerIsRunning() {
        assertTrue(CLUSTER.getAgentLeader().isRunning());
    }
}
```

## Container

### Up & Running

Container implements *startup strategy* and will be *available to TestContainer framework automatically* when database will be ready for accepting connections.

Check [here](https://www.testcontainers.org/features/startup_and_waits/) for more info about strategies.

### Auth

All authentication options are available as per [ArangoDB Docker description](https://hub.docker.com/_/arangodb).

*Without authentication or password or random password* configuration is **required** as per [docker image](https://hub.docker.com/_/arangodb).

#### Without Authentication

You can run ArangoDB without authentication by specifying with setter.

```java
@Testcontainers
class ArangoContainerTests {

    @Container
    private static final ArangoContainer<?> container = new ArangoContainer<>()
            .withoutAuth();

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
    private static final ArangoContainer<?> container = new ArangoContainer<>()
            .withPassword("mypass");

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
    private static final ArangoContainer<?> container = new ArangoContainer<>()
            .withRandomPassword();

    @Test
    void checkContainerIsRunning() {
        assertTrue(container.isRunning());
    }
}
```

## Cluster

You can run [ArangoDB cluster](https://www.arangodb.com/community-server/cluster/) as TestContainers.

Default cluster with 3 Agent nodes, 2 DBServer nodes and 2 Coordinator nodes is preconfigured for easy usage.

```java
@Testcontainers
class ArangoContainerTests {

    @Container
    private static final ArangoCluster CLUSTER = ArangoCluster.builder("arangodb:3.11.2")
            .withPassword("jjj")
            .build();

    @Test
    void checkContainerIsRunning() {
        CLUSTER.getHost();
        CLUSTER.getPort();
        CLUSTER.getUser();
        CLUSTER.getPassword();
    }
}
```

### Cluster Builder

You can build cluster with desired size via *ArangoClusterBuilder*.

You can check each container type via specified cluster container method.

```java
final ArangoCluster cluster = ArangoCluster.builder("arangodb:3.11.2")
            .withAgentNodes(3)              // 3 dbserver nodes in cluster by default
            .withDatabaseNodes(2)           // 2 dbserver nodes in cluster by default
            .withCoordinatorNodes(2)        // 2 coordinator nodes in cluster by default
            .build();
```

## License

This project licensed under the MIT - see the [LICENSE](LICENSE) file for details.

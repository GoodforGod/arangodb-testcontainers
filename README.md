# ArangoDB TestContainer

This is [*ArangoDB*](https://www.arangodb.com/) [TestContainer](https://www.testcontainers.org/) implementation (with TestContainer startup strategy support.

Run *ArangoDB* as container in your Java\Kotlin tests.

## Dependency :rocket:
**Gradle**
```groovy
dependencies {
    compile 'com.github.goodforgod:arangodb-testcontainer:1.0.0'
}
```

**Maven**
```xml
<dependency>
    <groupId>com.github.goodforgod</groupId>
    <artifactId>arangodb-testcontainer</artifactId>
    <version>1.0.0</version>
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
    private static final ArangoContainer container = new ArangoContainer().withoutAuthentication();

    @Test
    void checkContainerIsRunning() {
        assertTrue(container.isRunning());
    }
}
```

## Container

### Up & Running

Container implements *startup strategy* and will be *available to TestContainer framework automatically* when database will be ready for accepting connections.

Check [here](https://www.testcontainers.org/features/startup_and_waits/) for more info about strategies.

### Port

Container runs by default on *8529* port (this is default for ArangpDB).

You can specify desired port with specified setter.

```java
@Testcontainers
class ArangoContainerTests {

    @Container
    private static final ArangoContainer container = new ArangoContainer().setPort(5689);

    @Test
    void checkContainerIsRunning() {
        assertTrue(container.isRunning());
    }
```

### Auth

All authentication options are available as per [ArangoDB Docker description](https://hub.docker.com/_/arangodb).

#### Without Authentication

You can run ArangoDB without authentication by specifying with setter.

```java
@Testcontainers
class ArangoContainerTests {

    @Container
    private static final ArangoContainer container = new ArangoContainer().withoutAuthentication();

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

## Versions

**1.0.0** - Initial project with auth, port set, startup strategy.

## License

This project is licensed under the MIT - see the [LICENSE](LICENSE) file for details.


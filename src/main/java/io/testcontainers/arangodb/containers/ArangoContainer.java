package io.testcontainers.arangodb.containers;

import java.util.function.Consumer;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

/**
 * ArangoDB TestContainer docker container implementation. Uses Log4j as logger for container output.
 *
 * @author Anton Kurako (GoodforGod)
 * @since 2.3.2020
 */
public class ArangoContainer extends GenericContainer<ArangoContainer> {

    public static final String LATEST = "latest";
    private static final String IMAGE = "arangodb";

    private static final DockerImageName DEFAULT_IMAGE_NAME = DockerImageName.parse(IMAGE);

    public static final Integer PORT_DEFAULT = 8529;
    public static final String ROOT_USER = "root";

    private static final String ARANGO_NO_AUTH = "ARANGO_NO_AUTH";
    private static final String ARANGO_ROOT_PASSWORD = "ARANGO_ROOT_PASSWORD";
    private static final String ARANGO_RANDOM_ROOT_PASSWORD = "ARANGO_RANDOM_ROOT_PASSWORD";

    private String password;

    /**
     * This is recommended usage by TestContainers library
     * 
     * @see org.testcontainers.containers.GenericContainer
     * @deprecated use {@link ArangoContainer(String)} instead
     */
    @Deprecated
    public ArangoContainer() {
        this(LATEST);
    }

    public ArangoContainer(String version) {
        this(DockerImageName.parse(IMAGE).withTag(version));
    }

    public ArangoContainer(DockerImageName imageName) {
        super(imageName);
        imageName.assertCompatibleWith(DEFAULT_IMAGE_NAME);

        withLogConsumer(getOutputConsumer());
        waitingFor(Wait.forLogMessage(".*is ready for business. Have fun!.*", 1));
    }

    protected Consumer<OutputFrame> getOutputConsumer() {
        return new Slf4jLogConsumer(LoggerFactory.getLogger(getClass()));
    }

    /**
     * Setup desired password for {@link #ROOT_USER} for database.
     *
     * @param password to set on startup
     * @return container itself
     */
    public ArangoContainer withPassword(String password) {
        if (getEnvMap().containsKey(ARANGO_NO_AUTH) || getEnvMap().containsKey(ARANGO_RANDOM_ROOT_PASSWORD))
            throwAuthException();

        withEnv(ARANGO_ROOT_PASSWORD, password);
        this.password = password;
        return self();
    }

    /**
     * Setup ArangoDB to start without authentication.
     *
     * @return container itself
     */
    public ArangoContainer withoutAuth() {
        if (getEnvMap().containsKey(ARANGO_ROOT_PASSWORD) || getEnvMap().containsKey(ARANGO_RANDOM_ROOT_PASSWORD))
            throwAuthException();

        withEnv(ARANGO_NO_AUTH, "1");
        return self();
    }

    /**
     * Setup random password for {@link #ROOT_USER} for database on startup.
     *
     * @return container itself
     */
    public ArangoContainer withRandomPassword() {
        if (getEnvMap().containsKey(ARANGO_ROOT_PASSWORD) || getEnvMap().containsKey(ARANGO_NO_AUTH))
            throwAuthException();

        withEnv(ARANGO_RANDOM_ROOT_PASSWORD, "1");
        return self();
    }

    public String getPassword() {
        return password;
    }

    public String getUser() {
        return ROOT_USER;
    }

    public Integer getPort() {
        return getMappedPort(PORT_DEFAULT);
    }

    /**
     * Specify container port to run as instead of default one
     *
     * @param port to set for container to run at
     * @return container itself
     * @see #PORT_DEFAULT
     */
    public ArangoContainer withFixedPort(int port) {
        addFixedExposedPort(port, PORT_DEFAULT);
        return self();
    }

    /**
     * Turns off fixed port mapping and maps container to random host port.
     *
     * @return container self
     */
    public ArangoContainer withRandomPort() {
        return self();
    }

    private void throwAuthException() {
        throw new IllegalArgumentException(
                "Random or without authentication is enable, please review your configuration");
    }
}

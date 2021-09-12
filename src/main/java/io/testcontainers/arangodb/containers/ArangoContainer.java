package io.testcontainers.arangodb.containers;

import java.util.ArrayList;
import java.util.List;
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
    public static final Integer DEFAULT_PORT = 8529;
    public static final String DEFAULT_USER = "root";

    private static final String IMAGE = "arangodb";
    private static final DockerImageName DEFAULT_IMAGE_NAME = DockerImageName.parse(IMAGE);

    private static final String ARANGO_NO_AUTH = "ARANGO_NO_AUTH";
    private static final String ARANGO_ROOT_PASSWORD = "ARANGO_ROOT_PASSWORD";
    private static final String ARANGO_RANDOM_ROOT_PASSWORD = "ARANGO_RANDOM_ROOT_PASSWORD";

    private String password;

    public ArangoContainer(String version) {
        this(DockerImageName.parse(IMAGE).withTag(version));
    }

    public ArangoContainer(DockerImageName imageName) {
        super(imageName);
        imageName.assertCompatibleWith(DEFAULT_IMAGE_NAME);

        addExposedPort(DEFAULT_PORT);
        withLogConsumer(getOutputConsumer());
        waitingFor(Wait.forLogMessage(".*is ready for business. Have fun!.*", 1));
    }

    protected Consumer<OutputFrame> getOutputConsumer() {
        return new Slf4jLogConsumer(LoggerFactory.getLogger(getClass()));
    }

    /**
     * Setup desired password for {@link #DEFAULT_USER} for database.
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
     * Setup random password for {@link #DEFAULT_USER} for database on startup.
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
        return DEFAULT_USER;
    }

    public Integer getPort() {
        return getMappedPort(DEFAULT_PORT);
    }

    /**
     * Specify container port to run as instead of default one
     *
     * @param port to set for container to run at
     * @return container itself
     * @see #DEFAULT_PORT
     */
    public ArangoContainer withFixedPort(int port) {
        setExposedPorts(new ArrayList<>());
        addFixedExposedPort(port, DEFAULT_PORT);
        return self();
    }

    private void throwAuthException() {
        throw new IllegalArgumentException(
                "Random or without authentication is enable, please review your configuration");
    }

    @Override
    public void setExposedPorts(List<Integer> exposedPorts) {
        setPortBindings(new ArrayList<>());
        super.setExposedPorts(exposedPorts);
    }

    @Override
    public void addExposedPorts(int... ports) {
        setPortBindings(new ArrayList<>());
        super.addExposedPorts(ports);
    }

    @Override
    public void addExposedPort(Integer port) {
        setPortBindings(new ArrayList<>());
        super.addExposedPort(port);
    }
}

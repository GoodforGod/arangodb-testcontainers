package io.testcontainers.arangodb.containers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;

/**
 * ArangoDB TestContainer docker container implementation. Uses Log4j as logger for container output.
 *
 * @author Anton Kurako (GoodforGod)
 * @since 2.3.2020
 */
public class ArangoContainer extends GenericContainer<ArangoContainer> {

    private static final String IMAGE = "arangodb";
    private static final String LATEST_VERSION = "latest";

    public static final String HOST = "localhost";
    public static final Integer PORT_DEFAULT = 8529;
    public static final String ROOT_USER = "root";

    private static final String ARANGO_NO_AUTH = "ARANGO_NO_AUTH";
    private static final String ARANGO_ROOT_PASSWORD = "ARANGO_ROOT_PASSWORD";
    private static final String ARANGO_RANDOM_ROOT_PASSWORD = "ARANGO_RANDOM_ROOT_PASSWORD";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private String password;
    private Integer port = PORT_DEFAULT;
    private Integer internalPort = PORT_DEFAULT;

    private boolean enableAwaitStart = true;

    public ArangoContainer() {
        this(LATEST_VERSION);
    }

    public ArangoContainer(String version) {
        super(IMAGE + ":" + version);
    }

    protected ArangoContainer turnOffStart() {
        this.enableAwaitStart = false;
        return self();
    }

    protected ArangoContainer withInternalPort(Integer port) {
        this.internalPort = port;
        return self();
    }

    /**
     * Configures startup strategy to single TestContainer framework that container is ready to accept connections
     */
    @Override
    protected void configure() {
        withLogConsumer(new Slf4jLogConsumer(logger));

        if (port != null)
            addFixedExposedPort(port, internalPort);

        if (enableAwaitStart)
            waitingFor(Wait.forLogMessage(".*is ready for business. Have fun!.*\\n", 1));
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

    public Integer getPort() {
        return port;
    }

    /**
     * Specify container port to run as istead of default one
     *
     * @param port to set for container to run at
     * @return container itself
     * @see #PORT_DEFAULT
     */
    public ArangoContainer withPort(Integer port) {
        this.port = port;
        return self();
    }

    public String getUser() {
        return ROOT_USER;
    }

    public String getHost() {
        return HOST;
    }

    private void throwAuthException() {
        throw new IllegalArgumentException(
                "Random or without authentication is enable, please review your configuration");
    }
}

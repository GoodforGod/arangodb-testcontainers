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
public class ArangoContainer<SELF extends ArangoContainer<SELF>> extends GenericContainer<SELF> {

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
    private int port = PORT_DEFAULT;

    public ArangoContainer() {
        this(LATEST_VERSION);
    }

    private ArangoContainer(String version) {
        super(IMAGE + ":" + version);
    }

    /**
     * Configures startup strategy to single TestContainer framework that container is ready to accept connections
     */
    @Override
    protected void configure() {
        addFixedExposedPort(port, PORT_DEFAULT);
        withLogConsumer(new Slf4jLogConsumer(logger));
        waitingFor(Wait.forLogMessage(".*is ready for business. Have fun!.*\\n", 1));
    }

    /**
     * Setup desired password for {@link #ROOT_USER} for database.
     * 
     * @param password to set on startup
     * @return container itself
     */
    public SELF withPassword(String password) {
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
    public SELF withoutAuthentication() {
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
    public SELF withRandomPassword() {
        if (getEnvMap().containsKey(ARANGO_ROOT_PASSWORD) || getEnvMap().containsKey(ARANGO_NO_AUTH))
            throwAuthException();

        withEnv(ARANGO_RANDOM_ROOT_PASSWORD, "1");
        return self();
    }

    public String getPassword() {
        return password;
    }

    public int getPort() {
        return port;
    }

    /**
     * Specify container port to run as istead of default one
     * 
     * @see #PORT_DEFAULT
     * @param port to set for container to run at
     * @return container itself
     */
    public ArangoContainer<SELF> setPort(int port) {
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

package io.testcontainer.arangodb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;

/**
 * ArangoDB TestContainer docker container implementation.
 *
 * @author Anton Kurako (GoodforGod)
 * @since 2.3.2020
 */
public class ArangoDBContainer<SELF extends ArangoDBContainer<SELF>> extends GenericContainer<SELF> {

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

    public ArangoDBContainer() {
        this(LATEST_VERSION);
    }

    private ArangoDBContainer(String version) {
        super(IMAGE + ":" + version);
    }

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

    public Integer getPort() {
        return PORT_DEFAULT;
    }

    public ArangoDBContainer<SELF> setPort(int port) {
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

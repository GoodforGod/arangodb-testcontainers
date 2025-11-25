package io.testcontainers.arangodb.containers;

import java.time.Duration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

/**
 * ArangoDB TestContainer docker container implementation.
 *
 * @author Anton Kurako (GoodforGod)
 * @since 2.3.2020
 */
public class ArangoContainer extends GenericContainer<ArangoContainer> {

    public static final Integer PORT = 8529;

    private static final String DEFAULT_USER = "root";
    private static final String IMAGE_NAME = "arangodb";
    private static final DockerImageName IMAGE = DockerImageName.parse(IMAGE_NAME);

    private static final String ARANGO_NO_AUTH = "ARANGO_NO_AUTH";
    private static final String ARANGO_ROOT_PASSWORD = "ARANGO_ROOT_PASSWORD";
    private static final String ARANGO_RANDOM_ROOT_PASSWORD = "ARANGO_RANDOM_ROOT_PASSWORD";

    private String password;

    public ArangoContainer(String dockerImageName) {
        this(DockerImageName.parse(dockerImageName));
    }

    public ArangoContainer(DockerImageName imageName) {
        super(imageName);
        imageName.assertCompatibleWith(IMAGE);
        addExposedPort(PORT);
        waitingFor(Wait.forLogMessage(".*is ready for business. Have fun.*", 1));
        withStartupTimeout(Duration.ofSeconds(60));
    }

    @Override
    protected void configure() {
        if (getEnvMap().containsKey(ARANGO_ROOT_PASSWORD)) {
            if (getEnvMap().containsKey(ARANGO_NO_AUTH) || getEnvMap().containsKey(ARANGO_RANDOM_ROOT_PASSWORD)) {
                throwAuthException();
            }
        }

        if (getEnvMap().containsKey(ARANGO_NO_AUTH)) {
            if (getEnvMap().containsKey(ARANGO_ROOT_PASSWORD) || getEnvMap().containsKey(ARANGO_RANDOM_ROOT_PASSWORD)) {
                throwAuthException();
            }
        }

        if (getEnvMap().containsKey(ARANGO_RANDOM_ROOT_PASSWORD)) {
            if (getEnvMap().containsKey(ARANGO_ROOT_PASSWORD) || getEnvMap().containsKey(ARANGO_NO_AUTH)) {
                throwAuthException();
            }
        }

        super.configure();
    }

    /**
     * Setup desired password for {@link #DEFAULT_USER} for database.
     *
     * @param password to set on startup
     * @return container itself
     */
    public ArangoContainer withPassword(String password) {
        this.password = password;
        return withEnv(ARANGO_ROOT_PASSWORD, password);
    }

    /**
     * Setup ArangoDB to start without authentication.
     *
     * @return container itself
     */
    public ArangoContainer withoutAuth() {
        return withEnv(ARANGO_NO_AUTH, "1");
    }

    /**
     * Setup random password for {@link #DEFAULT_USER} for database on startup.
     *
     * @return container itself
     */
    public ArangoContainer withRandomPassword() {
        return withEnv(ARANGO_RANDOM_ROOT_PASSWORD, "1");
    }

    public String getPassword() {
        return password;
    }

    public String getUser() {
        return DEFAULT_USER;
    }

    public Integer getPort() {
        return getMappedPort(PORT);
    }

    private void throwAuthException() {
        throw new IllegalArgumentException(
                "Random or without authentication is enable, please review your configuration");
    }
}

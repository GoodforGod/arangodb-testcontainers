package io.testcontainers.arangodb;

import io.testcontainers.arangodb.containers.ArangoContainer;
import java.net.URI;
import org.junit.jupiter.api.Assertions;

/**
 * Description in progress
 *
 * @author Anton Kurako (GoodforGod)
 * @since 4.3.2020
 */
public abstract class ArangoRunner extends Assertions {

    protected static final String IMAGE_3_7 = "arangodb:3.7.13";
    protected static final String IMAGE_3_12 = "arangodb:3.12.4";

    protected URI getGetCheckURI(ArangoContainer container) {
        String host = (container.getHost().equals("[::1]"))
                ? "localhost"
                : container.getHost();
        return URI.create("http://" + host + ":" + container.getPort() + "/_api/collection");
    }
}

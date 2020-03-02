package io.testcontainer.arangodb;

import org.testcontainers.containers.GenericContainer;

/**
 * ArangoDB TestContainer docker container implementation.
 * https://hub.docker.com/_/arangodb
 *
 * @author Anton Kurako (GoodforGod)
 * @since 2.3.2020
 */
public class ArangoDBContainer<SELF extends ArangoDBContainer<SELF>> extends GenericContainer<SELF> {
}

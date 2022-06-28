package liquibase.resource;

import liquibase.plugin.Plugin;

import java.io.IOException;
import java.io.InputStream;

/**
 * Interface for extensions that can translate path strings into {@link ResourceAccessor}s and {@link java.io.InputStream}s.
 */
public interface PathHandler extends Plugin {

    /**
     * Priority of this parser for the given path. The implementation with the highest priority will be used.
     */
    int getPriority(String root);

    /**
     * Parse the given path and return a {@link ResourceAccessor} for it.
     *
     * @throws IOException if the path is invalid
     */
    ResourceAccessor getResourceAccessor(String root) throws IOException;

    /**
     * Parse the given path and return an {@link java.io.InputStream} for it.
     *
     * @throws IOException if the path is invalid
     */
    InputStream open(String path) throws IOException;
}

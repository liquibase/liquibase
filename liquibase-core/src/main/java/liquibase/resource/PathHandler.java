package liquibase.resource;

import liquibase.plugin.Plugin;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

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
     * @throws FileNotFoundException if the path is valid but does not exist
     */
    ResourceAccessor getResourceAccessor(String root) throws IOException, FileNotFoundException;

    /**
     * Parse the given "absolute" path and return a {@link liquibase.resource.Resource} for it if it exists.
     *
     * @return a Resource even if the resource does not exist. Callers can check {@link Resource#exists()} to determine if it exists or not
     * @throws IOException if the path is invalid
     */
    Resource getResource(String path) throws IOException;

    /**
     * Creates a new resource at the specified path and returns an OutputStream for writing to it.
     *
     * @throws java.nio.file.FileAlreadyExistsException if the file already exists
     * @throws IOException if the path cannot be written to
     */
    OutputStream createResource(String path) throws IOException;

}

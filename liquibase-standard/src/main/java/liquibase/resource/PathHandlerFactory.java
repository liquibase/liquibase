package liquibase.resource;

import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.plugin.AbstractPluginFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

/**
 * Singleton for working with {@link PathHandler}s.
 */
public class PathHandlerFactory extends AbstractPluginFactory<PathHandler> {

    private PathHandlerFactory() {
    }

    @Override
    protected Class<PathHandler> getPluginClass() {
        return PathHandler.class;
    }

    @Override
    protected int getPriority(PathHandler obj, Object... args) {
        return obj.getPriority((String) args[0]);
    }

    /**
     * Creates a {@link ResourceAccessor} for the given path.
     */
    public ResourceAccessor getResourceAccessor(String root) throws IOException {
        final PathHandler plugin = getPlugin(root);
        if (plugin == null) {
            throw new IOException("Cannot parse resource location: '" + root + "'");
        }
        return plugin.getResourceAccessor(root);
    }

    /**
     * Creates a new resource at the specified path and returns an OutputStream for writing to it.
     *
     * @throws java.nio.file.FileAlreadyExistsException if the file already exists
     * @throws IOException                              if the path cannot be written to
     */
    public OutputStream createResource(String resourcePath) throws IOException {
        final PathHandler plugin = getPlugin(resourcePath);
        if (plugin == null) {
            throw new IOException("Cannot parse resource location: '" + resourcePath + "'");
        }

        return plugin.createResource(resourcePath);
    }

    /**
     * Return the resource for the given path.
     *
     * @return A resource, regardless of whether it exists or not.
     * @throws IOException if the path cannot be understood or if there is a problem parsing the path
     * @throws IOException if the path exists as both a direct resourcePath and also in the resourceAccessor (if included). Unless {@link liquibase.GlobalConfiguration#DUPLICATE_FILE_MODE} overrides that behavior.
     */
    @SuppressWarnings("java:S2095")
    public Resource getResource(String resourcePath) throws IOException {
        final PathHandler plugin = getPlugin(resourcePath);
        if (plugin == null) {
            throw new IOException("Cannot parse resource location: '" + resourcePath + "'");
        }

        return plugin.getResource(resourcePath);
    }

    /**
     * Returns the outputStream from {@link #getResource(String)} if it exists, and the outputStream from {@link #createResource(String)} if it does not.
     *
     * @return null if resourcePath does not exist and createIfNotExists is false
     * @throws IOException if there is an error opening the stream
     *
     * @deprecated use {@link #openResourceOutputStream(String, OpenOptions)}
     */
    @Deprecated
    public OutputStream openResourceOutputStream(String resourcePath, boolean createIfNotExists) throws IOException {
        return openResourceOutputStream(resourcePath, new OpenOptions().setCreateIfNeeded(createIfNotExists));
    }

    /**
     * Returns the outputStream from {@link #getResource(String)}, using settings from the passed {@link OpenOptions}.
     *
     * @return null if resourcePath does not exist and {@link OpenOptions#isCreateIfNeeded()} is false
     * @throws IOException if there is an error opening the stream
     */
    public OutputStream openResourceOutputStream(String resourcePath, OpenOptions openOptions) throws IOException {
        Resource resource = getResource(resourcePath);
        if (!resource.exists()) {
            if (openOptions.isCreateIfNeeded()) {
                return createResource(resourcePath);
            } else {
                return null;
            }
        }
        return resource.openOutputStream(openOptions);
    }

    /**
     * Adapts a resource found by the PathHandlerFactory to the ResourceAccessor interface so it can be used
     * with the standard "duplicate file logic" in the ResourceAccessors
     *
     */
    private static class FoundResourceAccessor implements ResourceAccessor {

        private final Resource foundResource;
        private final String location;

        public FoundResourceAccessor(String location, Resource foundResource) {
            this.location = location;
            this.foundResource = foundResource;
        }

        @Override
        public List<Resource> search(String path, SearchOptions searchOptions) throws IOException {
            throw new UnexpectedLiquibaseException("Method not implemented");
        }

        @Override
        public List<Resource> search(String path, boolean recursive) throws IOException {
            throw new UnexpectedLiquibaseException("Method not implemented");
        }

        @Override
        public List<Resource> getAll(String path) throws IOException {
            if (foundResource == null || !foundResource.exists()) {
                return null;
            }

            return Collections.singletonList(foundResource);
        }

        @Override
        public List<String> describeLocations() {
            return Collections.singletonList(location);
        }

        @Override
        public void close() throws Exception {

        }
    }
}

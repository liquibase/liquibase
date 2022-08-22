package liquibase.resource;

import liquibase.Scope;
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
     * Convenience method for {@link #getResource(String, boolean)} with false for includeResourceAccessor
     */
    public Resource getResource(String resourcePath) throws IOException {
        return getResource(resourcePath, false);
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
     * @param includeResourceAccessor if true, check {@link Scope#getResourceAccessor()} as well.
     * @return null if the resource does not exist
     * @throws IOException if the path cannot be understood or if there is a problem parsing the path
     * @throws IOException if the path exists as both a direct resourcePath and also in the resourceAccessor (if included). Unless {@link liquibase.GlobalConfiguration#DUPLICATE_FILE_MODE} overrides that behavior.
     */
    public Resource getResource(String resourcePath, boolean includeResourceAccessor) throws IOException {
        final PathHandler plugin = getPlugin(resourcePath);
        if (plugin == null) {
            throw new IOException("Cannot parse resource location: '" + resourcePath + "'");
        }

        Resource foundResource = plugin.getResource(resourcePath);

        if (includeResourceAccessor) {
            try(ResourceAccessor resourceAccessor = new CompositeResourceAccessor(Scope.getCurrentScope().getResourceAccessor(), new FoundResourceAccessor(resourcePath, foundResource))) {

                Resource resource = resourceAccessor.get(resourcePath);
                if (!resource.exists()) {
                    return foundResource;
                }
                return resource;
            } catch (Exception e) {
                Scope.getCurrentScope().getLog(getClass()).fine("Error closing resourceAccessor: "+e.getMessage(), e);
            }
        } else {
            return foundResource;
        }
    }

    /**
     * Returns the outputStream from {@link #getResource(String, boolean)} if it exists, and the outputStream from {@link #createResource(String)} if it does not.
     *
     * @return null if resourcePath does not exist and createIfNotExists is false
     * @throws IOException if there is an error opening the stream
     */
    public OutputStream openResourceOutputStream(String resourcePath, boolean includeResourceAccessor, boolean createIfNotExists) throws IOException {
        Resource resource = getResource(resourcePath, includeResourceAccessor);
        if (!resource.exists()) {
            if (createIfNotExists) {
                return createResource(resourcePath);
            } else {
                return null;
            }
        }
        return resource.openOutputStream(createIfNotExists);
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

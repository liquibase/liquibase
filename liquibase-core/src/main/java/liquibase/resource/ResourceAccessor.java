package liquibase.resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * Abstracts file access so they can be read in a variety of manners.
 */
public interface ResourceAccessor {

    /**
     * Return an InputStream for each resource mapped by the given path.
     * The path is often a URL but does not have to be.
     *
     * @return null if the resource does not exist.
     * @throws IOException if there is an error reading an existing path.
     */
    public Set<InputStream> getResourcesAsStream(String path) throws IOException;

    /**
     * Returns the path to all resources contained in the given root.
     * The passed root is not included in the returned set.
     *
     * @return null if the root does not exist.
     * @throws IOException if there is an error reading an existing root.
     *
     * @param includeFiles Set to false to exclude files in the returned set. Defaults to true
     * @param includeDirectories Set to false to exclude directories in the returned set. Defaults to true
     * @param recursive Set to true and will return paths to contents in sub directories as well. Defaults to false
     */
    public Set<String> list(String relativeTo, String path, boolean includeFiles, boolean includeDirectories, boolean recursive) throws IOException;

    public ClassLoader toClassLoader();
}

package liquibase.resource;

import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.logging.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * ResourceAccessors abstract file access so they can be read in a variety of environments.
 * Implementations may look for local files in known locations, read from the network, or do whatever else they need to find files.
 * Think of ResourceAccessors as a {@link ClassLoader} but for finding and reading files, not finding and loading classes.
 */
public interface ResourceAccessor extends AutoCloseable {

    /**
     * Return the streams for each resource mapped by the given path.
     * The path is often a URL but does not have to be.
     * Should accept both / and \ chars for file paths to be platform-independent.
     * If path points to a compressed resource, return a stream of the uncompressed contents of the file.
     * Returns {@link InputStreamList} since multiple resources can map to the same path, such as "META-INF/MAINFEST.MF".
     * Remember to close streams when finished with them.
     *
     * @return Empty list if the resource does not exist.
     * @throws IOException if there is an error reading an existing path.
     */
    InputStreamList openStreams(String relativeTo, String streamPath) throws IOException;

    /**
     * Returns a single stream matching the given path. See {@link #openStreams(String, String)} for details about path options.
     * Implementations should respect {@link liquibase.GlobalConfiguration#DUPLICATE_FILE_MODE}
     *
     * @param relativeTo Location that streamPath should be found relative to. If null, streamPath is an absolute path
     * @return null if the resource does not exist
     * @throws IOException if multiple paths matched the stream
     * @throws IOException if there is an error reading an existing path
     */
    InputStream openStream(String relativeTo, String streamPath) throws IOException;

//    /**
//     * Returns the path to all resources contained in the given path.
//     * The passed path is not included in the returned set.
//     * Returned strings should use "/" for file path separators, regardless of the OS and should accept both / and \ chars for file paths to be platform-independent.
//     * Returned set is sorted, normally alphabetically but subclasses can use different comparators.
//     * The values returned should be able to be passed into {@link #openStreams(String)} and return the contents.
//     * Returned paths should normally be root-relative and therefore not be an absolute path, unless there is a good reason to be absolute.
//     *
//     *
//     * @param relativeTo Location that streamPath should be found relative to. If null, path is an absolute path
//     * @param path The path to lookup resources in.
//     * @param recursive Set to true and will return paths to contents in sub directories as well.
//     * @param includeFiles Set to true and will return paths to files.
//     * @param includeDirectories Set to true and will return paths to directories.
//     * @return empty set if nothing was found
//     * @throws IOException if there is an error reading an existing root.
//     */
//    SortedSet<String> list(String relativeTo, String path, boolean recursive, boolean includeFiles, boolean includeDirectories) throws IOException;

    /**
     * Finds all files
     */
    List<Resource> list(String path, boolean recursive) throws IOException;

    List<Resource> getAll(String path) throws IOException;

    /**
     * Finds a single specific file. If multiple files match, handle based on the {@link GlobalConfiguration#DUPLICATE_FILE_MODE} setting.
     */
    default Resource get(String path) throws IOException {
        List<Resource> resources = getAll(path);

        if (resources == null || resources.size() == 0) {
            return null;
        } else if (resources.size() == 1) {
            return resources.iterator().next();
        } else {
            String message = "Found " + resources.size() + " files with the path '" + path + "':" + System.lineSeparator();
            for (Resource resource : resources) {
                message += "    - " + resource.getUri() + System.lineSeparator();
            }
            message += "  Search Path: " + System.lineSeparator();
            for (String location : Scope.getCurrentScope().getResourceAccessor().describeLocations()) {
                message += "    - " + location + System.lineSeparator();
            }
            message += "  You can limit the search path to remove duplicates with the liquibase.searchPath setting.";

            final GlobalConfiguration.DuplicateFileMode mode = GlobalConfiguration.DUPLICATE_FILE_MODE.getCurrentValue();
            final Logger log = Scope.getCurrentScope().getLog(getClass());

            if (mode == GlobalConfiguration.DuplicateFileMode.ERROR) {
                throw new IOException(message + " Or, if you KNOW these are the exact same file you can set liquibase.duplicateFileMode=WARN.");
            } else if (mode == GlobalConfiguration.DuplicateFileMode.WARN) {
                Resource resource = resources.iterator().next();
                final String warnMessage = message + System.lineSeparator() +
                        "  To fail when duplicates are found, set liquibase.duplicateFileMode=ERROR" + System.lineSeparator() +
                        "  Choosing: " + resource.getUri();
                Scope.getCurrentScope().getUI().sendMessage(warnMessage);
                log.warning(warnMessage);
            }

            return resources.iterator().next();
        }
    }

    /**
     * Returns a description of the places this classloader will look for paths. Used in error messages and other troubleshooting cases.
     */
    List<String> describeLocations();

}

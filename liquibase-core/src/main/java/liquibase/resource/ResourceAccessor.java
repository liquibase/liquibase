package liquibase.resource;

import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.logging.Logger;
import liquibase.util.CollectionUtil;
import liquibase.util.FileUtil;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

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
     * @param relativeTo Location that streamPath should be found relative to. If null, streamPath is an absolute path
     * @return Empty list if the resource does not exist.
     * @throws IOException if there is an error reading an existing path.
     * @deprecated Use {@link #search(String, boolean)} or {@link #getAll(String)}
     */
    @SuppressWarnings("java:S2095")
    @Deprecated
    default InputStreamList openStreams(String relativeTo, String streamPath) throws IOException {
        InputStreamList returnList = new InputStreamList();

        for (Resource resource : CollectionUtil.createIfNull(getAll(resolve(relativeTo, streamPath)))) {
            returnList.add(resource.getUri(), resource.openInputStream());
        }

        return returnList;
    }

    /**
     * Returns a single stream matching the given path. See {@link #openStreams(String, String)} for details about path options.
     * Implementations should respect {@link liquibase.GlobalConfiguration#DUPLICATE_FILE_MODE}
     *
     * @param relativeTo Location that streamPath should be found relative to. If null, streamPath is an absolute path
     * @return null if the resource does not exist
     * @throws IOException if multiple paths matched the stream
     * @throws IOException if there is an error reading an existing path
     * @deprecated Use {@link #search(String, boolean)} or {@link #getAll(String)}
     */
    @Deprecated
    default InputStream openStream(String relativeTo, String streamPath) throws IOException {
        Resource resource = get(resolve(relativeTo, streamPath));
        if (resource == null) {
            return null;
        }
        return resource.openInputStream();

    }

    /**
     * Returns the final path of a file at the given path relative to a <b>file</b> at relativeTo.
     * Therefore, the path is within relativeTo's directory.
     * If relativeTo is null, return path.
     * This method should return a value even if relativeTo or path do not exist.
     */
    default String resolve(String relativeTo, String path) {
        if (relativeTo == null) {
            return path;
        }
        Path basePath = Paths.get(relativeTo).getParent();
        if (basePath == null) {
            return path;
        }

        return basePath.resolve(path).normalize().toString().replace("\\", "/");
    }

    /**
     * Returns the path to all resources contained in the given path.
     * The passed path is not included in the returned set.
     * Returned strings should use "/" for file path separators, regardless of the OS and should accept both / and \ chars for file paths to be platform-independent.
     * Returned set is sorted, normally alphabetically but subclasses can use different comparators.
     * The values returned should be able to be passed into {@link #openStreams(String, String)} and return the contents.
     * Returned paths should normally be root-relative and therefore not be an absolute path, unless there is a good reason to be absolute.
     * <p>
     * Default implementation calls {@link #search(String, boolean)} and collects the paths from the resources.
     * Because the new method no longer supports listing directories, it will silently ignore the includeDirectories argument UNLESS includeFiles is false. In that case, it will throw an exception.
     *
     * @param relativeTo         Location that streamPath should be found relative to. If null, path is an absolute path
     * @param path               The path to lookup resources in.
     * @param recursive          Set to true and will return paths to contents in sub directories as well.
     * @param includeFiles       Set to true and will return paths to files.
     * @param includeDirectories Set to true and will return paths to directories.
     * @return empty set if nothing was found
     * @throws IOException if there is an error reading an existing root.
     * @deprecated use {@link #search(String, boolean)}
     */
    @Deprecated
    default SortedSet<String> list(String relativeTo, String path, boolean recursive, boolean includeFiles, boolean includeDirectories) throws IOException {
        SortedSet<String> returnList = new TreeSet<>();
        if (includeFiles) {
            for (Resource resource : search(resolve(relativeTo, path), recursive)) {
                returnList.add(resource.getPath());
            }
        } else {
            throw new UnexpectedLiquibaseException("ResourceAccessor can no longer search only for directories");
        }
        return returnList;
    }

    /**
     * Returns the path to all resources contained in the given path.
     * Multiple resources may be returned with the same path, but only if they are actually unique files.
     * Order is important to pay attention to, they should be returned in a user-expected manner based on this resource accessor.
     * <br><br>
     * Should return an empty list if:
     * <ul>
     *     <li>Path does not exist</li>
     * </ul>
     * Should throw an exception if:
     * <ul>
     *     <li>Path is null</li>
     *     <li>Path is not a "directory"</li>
     *     <li>Path exists but cannot be read from</li>
     * </ul>
     *
     * @param path      The path to lookup resources in.
     * @param recursive Set to true and will return paths to contents in subdirectories as well.
     * @return empty set if nothing was found
     * @throws IOException if there is an error searching the system.
     */
    List<Resource> search(String path, boolean recursive) throws IOException;

    /**
     * Returns all {@link Resource}s at the given path.
     * For many resource accessors (such as a file system), only one resource can exist at a given spot,
     * but some accessors (such as {@link CompositeResourceAccessor} or {@link ClassLoaderResourceAccessor}) can have multiple resources for a single path.
     * <p>
     * If the resourceAccessor returns multiple values, the returned List should be considered sorted for that resource accessor.
     * For example, {@link ClassLoaderResourceAccessor} returns them in order based on the configured classloader.
     * Order is important to pay attention to, because users may set {@link GlobalConfiguration#DUPLICATE_FILE_MODE} to pick the "best" file which is defined as
     * "the first file from this function".
     * <p>
     *
     * @return null if no resources match the path
     * @throws IOException if there is an unexpected error determining what is at the path
     */
    List<Resource> getAll(String path) throws IOException;

    /**
     * Convenience version of {@link #get(String)} which throws an exception if the file does not exist.
     *
     * @throws FileNotFoundException if the file does not exist
     */
    default Resource getExisting(String path) throws IOException {
        Resource resource = get(path);
        if (resource == null) {
            throw new FileNotFoundException(FileUtil.getFileNotFoundMessage(path));
        }
        return resource;
    }

    /**
     * Finds a single specific {@link }. If multiple files match the given path, handle based on the {@link GlobalConfiguration#DUPLICATE_FILE_MODE} setting.
     * Default implementation calls {@link #getAll(String)}.
     *
     * @return null if the path does not exist
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

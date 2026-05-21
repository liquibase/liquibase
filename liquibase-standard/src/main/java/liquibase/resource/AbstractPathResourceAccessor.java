package liquibase.resource;

import liquibase.Scope;
import liquibase.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

/**
 * Base class for {@link ResourceAccessor}s that resolve a caller-supplied path against a
 * configured root directory (file system, zip, etc.).
 * <p>
 * <b>Containment guarantee.</b> Both {@link #getAll(String)} and
 * {@link #search(String, SearchOptions)} reject any path that, after normalisation, escapes
 * the configured {@link #getRootPath()}. They additionally verify the canonical real path
 * (after symbolic-link resolution) stays within the canonical root, so a symlink located
 * inside the root that targets a file outside it is not silently followed.
 * <p>
 * <b>Caveats.</b>
 * <ul>
 *   <li>The containment check is evaluated at {@code getAll} / {@code search} time. A
 *       symlink created or modified after the check (TOCTOU race) is not re-validated
 *       when the returned {@link Resource}'s stream is opened later.</li>
 *   <li>For {@link #search(String, SearchOptions)}, each visited file or directory is
 *       re-checked individually so symlinks discovered during the walk are skipped
 *       rather than aborting the whole walk.</li>
 * </ul>
 */
public abstract class AbstractPathResourceAccessor extends AbstractResourceAccessor {

    abstract protected Path getRootPath();

    @Override
    public String toString() {
        return getClass().getName() + " (" + getRootPath() + ")";
    }

    @Override
    public List<String> describeLocations() {
        return Collections.singletonList(getRootPath().toString());
    }

    @Override
    public List<Resource> getAll(String path) throws IOException {
        if (path == null) {
            throw new IllegalArgumentException("Path must not be null");
        }
        final List<Resource> returnList = new ArrayList<>();

        Logger log = Scope.getCurrentScope().getLog(getClass());
        path = standardizePath(path);

        if (path == null) {
            return returnList;
        }
        // Hoist the normalised root once so the containment check and error message
        // don't recompute it. DirectoryResourceAccessor stores the root as
        // normalize()+toAbsolutePath() at construction, so normalize() here is a
        // no-op for that subclass — done defensively for other implementations.
        Path rootPath = getRootPath().normalize();
        Path finalPath = rootPath.resolve(path).normalize();
        // Reject any payload that, after normalization, escapes the configured root.
        // Java's Path.normalize() on a relative path leaves leading ".." segments intact,
        // so a payload like "../../etc/passwd" survives standardizePath() and only
        // collapses after resolve(); a startsWith check catches it before Files.exists
        // would resolve it through the OS layer.
        if (!finalPath.startsWith(rootPath)) {
            throw new IOException("Path '" + path + "' resolves outside accessor root '" + rootPath + "'");
        }
        if (Files.exists(finalPath)) {
            // Defense in depth: a symbolic link at finalPath could point outside the
            // configured root. toRealPath() follows symlinks; reject if the canonical
            // location escapes the canonical root.
            if (!finalPath.toRealPath().startsWith(rootPath.toRealPath())) {
                throw new IOException("Path '" + path + "' resolves outside accessor root via symlink");
            }
            returnList.add(createResource(finalPath, path));
        } else {
            log.fine("Path " + path + " in " + rootPath + " does not exist (" + this + ")");
        }

        return returnList;
    }

    private String standardizePath(String path) {
        try {
            //
            // Flip the separators to Linux-style and replace the first separator
            // If then root path is the absolute path for Linux or Windows then return that result
            //
            String rootPath = getRootPath().toString();
            String result = new File(path).toPath().normalize().toString().replace("\\", "/").replaceFirst("^/", "");
            if (rootPath.equals("/") || rootPath.equals("\\")) {
                return result;
            }

            //
            // Strip off any Windows drive prefix and return the result
            //
            return result.replaceFirst("^\\w:/","");
        } catch (InvalidPathException e) {
            Scope.getCurrentScope().getLog(getClass()).warning("Failed to standardize path " + path, e);
            return path;
        }
    }

    @Override
    public List<Resource> search(String startPath, SearchOptions searchOptions) throws IOException {
        final int minDepth = searchOptions.getMinDepth();
        final int maxDepth = searchOptions.getMaxDepth();
        final boolean endsWithFilterIsSet = searchOptions.endsWithFilterIsSet();
        final String endsWithFilter = searchOptions.getEndsWithFilter();

        if (startPath == null) {
            throw new IllegalArgumentException("Path must not be null");
        }

        // Strip the file: scheme first (standardizePath() does not), then route startPath
        // through the same separator/drive normalisation as getAll() so behaviour matches
        // across both entry points (e.g. mixed-separator payloads like "..\\..\\..\\" on
        // Linux are recognised as traversal, not treated as a literal filename).
        startPath = standardizePath(startPath.replaceFirst("^file:/+", ""));
        Logger log = Scope.getCurrentScope().getLog(getClass());

        Path rootPath = getRootPath().normalize();
        Path basePath = rootPath.resolve(startPath).normalize();
        if (!basePath.startsWith(rootPath)) {
            throw new IOException("Search startPath '" + startPath
                    + "' resolves outside accessor root '" + rootPath + "'");
        }

        final List<Resource> returnSet = new ArrayList<>();
        if (!Files.exists(basePath)) {
            log.fine("Path " + startPath + " in " + rootPath + " does not exist (" + this + ")");
            return returnSet;
        }

        if (!Files.isDirectory(basePath)) {
            throw new IOException("'" + startPath + "' is a file, not a directory");
        }

        // Cache the canonical root once so the per-entry containment check below
        // doesn't re-resolve it on every visited file/directory.
        final Path rootRealPath = rootPath.toRealPath();

        SimpleFileVisitor<Path> fileVisitor = new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                // walkFileTree is invoked with FOLLOW_LINKS so subdirectories may be
                // reached via symlinks. Skip any subtree whose canonical location
                // escapes the canonical root.
                if (!dir.toRealPath().startsWith(rootRealPath)) {
                    log.fine("Skipping directory '" + dir + "' that resolves outside accessor root");
                    return FileVisitResult.SKIP_SUBTREE;
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                // Per-file symlink check (e.g. file-level link pointing outside the root).
                if (!file.toRealPath().startsWith(rootRealPath)) {
                    log.fine("Skipping file '" + file + "' that resolves outside accessor root");
                    return FileVisitResult.CONTINUE;
                }
                if (attrs.isRegularFile() && meetsSearchCriteria(file)) {
                    addToReturnList(file);
                }

                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            private boolean meetsSearchCriteria(Path file) {
                final int depth = file.getParent().getNameCount() - basePath.getNameCount() + 1;

                if (depth < minDepth) {
                    return false;
                }

                if (endsWithFilterIsSet) {
                    if (!file.toString().toLowerCase().endsWith(endsWithFilter.toLowerCase())) {
                        return false;
                    }
                }

                return true;
            }

            private void addToReturnList(Path file) {
                String pathToAdd = rootPath.relativize(file).normalize().toString().replace("\\", "/");

                pathToAdd = pathToAdd.replaceFirst("/$", "");
                returnSet.add(createResource(file, pathToAdd));
            }
        };

        Files.walkFileTree(basePath, Collections.singleton(FileVisitOption.FOLLOW_LINKS), maxDepth, fileVisitor);

        return returnSet;
    }

    @Override
    public List<Resource> search(String startPath, boolean recursive) throws IOException {
        SearchOptions searchOptions = new SearchOptions();

        searchOptions.setRecursive(recursive);

        return search(startPath, searchOptions);
    }

    protected abstract Resource createResource(Path file, String pathToAdd);

}

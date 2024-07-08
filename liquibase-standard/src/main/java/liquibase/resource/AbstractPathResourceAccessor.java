package liquibase.resource;

import liquibase.Scope;
import liquibase.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

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
        Path finalPath = getRootPath().resolve(path);
        if (Files.exists(finalPath)) {
            returnList.add(createResource(finalPath, path));
        } else {
            log.fine("Path " + path + " in " + getRootPath() + " does not exist (" + this + ")");
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

        startPath = startPath
                .replaceFirst("^file:/+", "")
                .replaceFirst("^/", "");
        Logger log = Scope.getCurrentScope().getLog(getClass());

        Path rootPath = getRootPath();
        Path basePath = rootPath.resolve(startPath);

        final List<Resource> returnSet = new ArrayList<>();
        if (!Files.exists(basePath)) {
            log.fine("Path " + startPath + " in " + rootPath + " does not exist (" + this + ")");
            return returnSet;
        }

        if (!Files.isDirectory(basePath)) {
            throw new IOException("'" + startPath + "' is a file, not a directory");
        }

        SimpleFileVisitor<Path> fileVisitor = new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
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

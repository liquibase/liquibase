package liquibase.resource;

import liquibase.Scope;

import java.io.*;
import java.nio.file.*;

/**
 * A @{link ResourceAccessor} implementation for files on the file system.
 * Will look for files in zip and jar files if they are added as root paths.
 */
public class DirectoryResourceAccessor extends AbstractPathResourceAccessor {

    private final Path rootDirectory;

    /**
     * Creates a FileSystemResourceAccessor with the given directory as the root.
     */
    public DirectoryResourceAccessor(File directory) throws FileNotFoundException {
        this(directory.toPath());
    }

    /**
     * Creates a FileSystemResourceAccessor with the given directory as the root.
     */
    public DirectoryResourceAccessor(Path directory) throws FileNotFoundException {
        if (directory == null) {
            throw new IllegalArgumentException("Directory must not be null");
        }
        directory = directory.normalize().toAbsolutePath();
        Scope.getCurrentScope().getLog(getClass()).fine("Creating resourceAccessor for directory " + directory);
        this.rootDirectory = directory;
        if (!Files.exists(directory)) {
            throw new FileNotFoundException("Non-existent directory: " + directory.toAbsolutePath());
        }
        if (!Files.isDirectory(directory)) {
            throw new IllegalArgumentException("Not a directory: " + directory.toAbsolutePath());
        }
    }

    @Override
    public void close() throws Exception {
        //nothing to close
    }

    @Override
    protected Path getRootPath() {
        return rootDirectory;
    }

    @Override
    protected Resource createResource(Path file, String pathToAdd) {
        return new PathResource(pathToAdd, file);
    }

}

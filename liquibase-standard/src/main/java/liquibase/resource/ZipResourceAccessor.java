package liquibase.resource;

import liquibase.Scope;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.nio.file.*;
import java.util.Collections;
import java.util.List;

public class ZipResourceAccessor extends AbstractPathResourceAccessor {

    private FileSystem fileSystem;

    /**
     * Creates a FileSystemResourceAccessor with the given directories/files as the roots.
     */
    public ZipResourceAccessor(File file) throws FileNotFoundException {
        this(file.toPath());
    }

    public ZipResourceAccessor(Path file) throws FileNotFoundException {
        this(file, new String[0]);
    }

    protected ZipResourceAccessor(Path file, String[] embeddedPaths) throws FileNotFoundException {
        if (file == null) {
            throw new IllegalArgumentException("File must not be null");
        }
        Scope.getCurrentScope().getLog(getClass()).fine("Creating resourceAccessor for file " + file);
        if (!Files.exists(file)) {
            throw new FileNotFoundException("Non-existent file: " + file.toAbsolutePath());
        }
        if (!Files.isRegularFile(file)) {
            throw new IllegalArgumentException("Not a regular file: " + file.toAbsolutePath());
        }
        String lowerCaseName = file.toString().toLowerCase();
        if (!(lowerCaseName.endsWith(".jar") || lowerCaseName.endsWith(".zip"))) {
            throw new IllegalArgumentException("Not a jar or zip file: " + file.toAbsolutePath());
        }

        URI finalUri = URI.create("jar:" + file.toUri());

        try {
            try {
                this.fileSystem = FileSystems.getFileSystem(finalUri);
            } catch (FileSystemNotFoundException e) {
                try {
                    this.fileSystem = FileSystems.newFileSystem(finalUri, Collections.emptyMap(), Scope.getCurrentScope().getClassLoader());
                } catch (FileSystemAlreadyExistsException ex) {
                    this.fileSystem = FileSystems.getFileSystem(finalUri);
                }
            }

            for (String embeddedPath : embeddedPaths) {
                Path innerPath = fileSystem.getPath(embeddedPath);
                try {
                    this.fileSystem = FileSystems.newFileSystem(innerPath, null);
                } catch (FileSystemNotFoundException e) {
                    this.fileSystem = FileSystems.newFileSystem(innerPath, Scope.getCurrentScope().getClassLoader());
                }
            }
        } catch (Throwable e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    @Override
    public void close() throws Exception {
        //can't close the filesystem because they often get reused and/or are being used by other things
    }

    @Override
    protected Path getRootPath() {
        return this.fileSystem.getPath("/");
    }

    @Override
    protected Resource createResource(Path file, String pathToAdd) {
        return new PathResource(pathToAdd, file);
    }

    @Override
    public List<String> describeLocations() {
        return Collections.singletonList(fileSystem.toString());
    }

    @Override
    public String toString() {
        return getClass().getName() + " (" + getRootPath() + ") (" + fileSystem.toString() + ")";
    }
}

package liquibase.resource;

import liquibase.Scope;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;

public class PathResource extends AbstractResource {

    private final Path path;

    public PathResource(String logicalPath, Path path) {
        super(logicalPath, path.normalize().toUri());
        this.path = path.normalize();
    }

    @SuppressWarnings("java:S2095")
    @Override
    public InputStream openInputStream() throws IOException {
        if (!Files.exists(this.path)) {
            throw new FileNotFoundException(this.path + " does not exist");
        } else if (Files.isDirectory(this.path)) {
            throw new FileNotFoundException(this.path + " is a directory");
        } else {
            InputStream stream = Files.newInputStream(this.path);

            if (this.path.getFileName().toString().toLowerCase().endsWith(".gz")) {
                stream = new GZIPInputStream(stream);
            }

            return stream;
        }
    }

    @Override
    public boolean exists() {
        return Files.exists(path);
    }

    @Override
    public Resource resolve(String other) {
        return new PathResource(resolvePath(other), path.resolve(other));
    }

    @Override
    public Resource resolveSibling(String other) {
        return new PathResource(resolveSiblingPath(other), path.resolveSibling(other));
    }

    @Override
    public boolean isWritable() {
        return !Files.isDirectory(this.path) && Files.isWritable(path);
    }

    @Override
    public OutputStream openOutputStream(boolean createIfNeeded) throws IOException {
        return openOutputStream(createIfNeeded, OpenOption.TRUNCATE);
    }

    @Override
    public OutputStream openOutputStream(boolean createIfNeeded, OpenOption openOption) throws IOException {
        if (!exists()) {
            if (createIfNeeded) {
                Path parent = path.getParent();
                if (parent != null) {
                    boolean mkdirs = parent.toFile().mkdirs();
                    if (!mkdirs) {
                        Scope.getCurrentScope().getLog(getClass()).warning("Failed to create parent directories for file " + path);
                    }
                }
            } else {
                throw new IOException("File " + this.getUri() + " does not exist");
            }
        }

        if (Files.isDirectory(this.path)) {
            throw new FileNotFoundException(this.getPath() + " is a directory");
        } else {
            return Files.newOutputStream(this.path, openOption.getStandardOpenOption());
        }
    }
}

package liquibase.resource;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;

public class PathResource extends AbstractResource {

    private final Path path;

    public PathResource(String logicalPath, Path path) {
        super(logicalPath, path.toUri());
        this.path = path;
    }

    @Override
    public boolean exists() {
        return Files.exists(path);
    }

    @Override
    public InputStream openInputStream() throws IOException {
        if (!this.exists()) {
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
    public boolean isWritable() {
        return !Files.isDirectory(this.path) && Files.isWritable(path);
    }

    @Override
    public OutputStream openOutputStream() throws IOException {
        if (Files.isDirectory(this.path)) {
            throw new FileNotFoundException(this.getPath() + " is a directory");
        } else {
            return Files.newOutputStream(this.path);
        }
    }
}

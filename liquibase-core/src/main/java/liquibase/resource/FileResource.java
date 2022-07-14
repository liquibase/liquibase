package liquibase.resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileResource extends AbstractResource {
    private final Path file;

    public FileResource(String path, File file) {
        this(path, file.toPath());
    }

    public FileResource(String path, Path file) {
        super(path);
        this.file = file;
    }

    @Override
    public InputStream openInputStream() throws IOException {
        return Files.newInputStream(file);
    }

    @Override
    public OutputStream openOutputStream() throws IOException {
        return Files.newOutputStream(file, openOptions);
    }
}

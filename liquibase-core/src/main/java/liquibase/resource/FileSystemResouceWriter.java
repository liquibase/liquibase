package liquibase.resource;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;

public class FileSystemResouceWriter implements ResourceWriter {

    private Path rootPath;

    public FileSystemResouceWriter() {
        rootPath = FileSystems.getDefault().getPath(".").toAbsolutePath();
    }

    public FileSystemResouceWriter(Path root) throws IOException {
        this();
        rootPath = root;
    }

    @Override
    public Path getPath(String path) {
        return rootPath.resolve(path);
    }

}

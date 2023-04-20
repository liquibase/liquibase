package liquibase.resource;

import java.io.*;
import java.nio.file.*;

/**
 * {@link PathHandler} that converts the path into a {@link DirectoryResourceAccessor}.
 */
public class DirectoryPathHandler extends AbstractPathHandler {

    /**
     * Returns {@link #PRIORITY_DEFAULT} for all paths except for ones that are for a non-"file:" protocol.
     */
    @Override
    public int getPriority(String root) {
        if (root == null) {
            return PRIORITY_NOT_APPLICABLE;
        }

        if (root.startsWith("/") || !root.contains(":")) {
            return PRIORITY_DEFAULT;
        }

        if (root.startsWith("file:") || root.matches("^[A-Za-z]:.*")) {
            return PRIORITY_DEFAULT;
        } else {
            return PRIORITY_NOT_APPLICABLE;
        }
    }

    @Override
    public ResourceAccessor getResourceAccessor(String root) throws FileNotFoundException {
        root = root
                .replace("file:", "")
                .replace("\\", "/");
        return new DirectoryResourceAccessor(new File(root));
    }

    @Override
    public Resource getResource(String path) throws IOException {
        return new PathResource(path, Paths.get(path));
    }

    @Override
    public OutputStream createResource(String path) throws IOException {
        Path path1 = Paths.get(path);
        // Need to create parent directories, because Files.newOutputStream won't create them.
        Path parent = path1.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }
        return Files.newOutputStream(path1, StandardOpenOption.CREATE_NEW);
    }
}
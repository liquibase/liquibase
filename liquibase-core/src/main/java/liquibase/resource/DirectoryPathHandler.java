package liquibase.resource;

import liquibase.Scope;
import liquibase.util.FilenameUtil;

import java.io.*;
import java.nio.file.*;

/**
 * {@link PathHandler} that converts the path into a {@link DirectoryResourceAccessor}.
 */
public class DirectoryPathHandler extends AbstractPathHandler {

    /**
     * Returns {@link #PRIORITY_DEFAULT} for all paths except for ones that are for a non-"file:" protocol.
     */
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

    public ResourceAccessor getResourceAccessor(String root) throws FileNotFoundException {
        root = root
                .replace("file:", "")
                .replace("\\", "/");
        return new DirectoryResourceAccessor(new File(root));
    }

    @Override
    public Resource getResource(String path) throws IOException {
        Path pathObj = Paths.get(path);
        if (!pathObj.toFile().exists()) {
            return null;
        }
        return new PathResource(path, pathObj);
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

    @Override
    public boolean exists(String path) {
        return Files.exists(Paths.get(path));
    }

    @Override
    public String concat(String parent, String objects) {
        return FilenameUtil.concat(parent, objects);
    }
}

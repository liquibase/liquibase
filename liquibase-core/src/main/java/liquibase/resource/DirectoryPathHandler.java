package liquibase.resource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    public ResourceAccessor getResourceAccessor(String root) {
        root = root.replace("file:", "");
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
}

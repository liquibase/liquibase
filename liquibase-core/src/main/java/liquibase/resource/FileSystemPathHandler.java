package liquibase.resource;

import liquibase.Scope;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;

/**
 * {@link PathHandler} that converts the path into a {@link FileSystemResourceAccessor}.
 */
public class FileSystemPathHandler extends AbstractPathHandler {

    /**
     * Returns {@link #PRIORITY_DEFAULT} for all paths except for ones that are for a non-"file:" protocol.
     */
    public int getPriority(String root) {
        if (root == null) {
            return PRIORITY_NOT_APPLICABLE;
        }

        if (!root.startsWith("/") && root.contains(":")) {
            if (root.startsWith("file:") || root.matches("^[A-Za-z]:.*")) {
                return PRIORITY_DEFAULT;
            } else {
                return PRIORITY_NOT_APPLICABLE;
            }
        }
        return PRIORITY_DEFAULT;
    }

    public ResourceAccessor getResourceAccessor(String root) {
        return new FileSystemResourceAccessor(new File(root.replace("\\", "/")));
    }
    @Override
    public InputStream open(String path) throws IOException {
        try {
            return Files.newInputStream(Paths.get(path));
        } catch (NoSuchFileException e) {
            Scope.getCurrentScope().getLog(FileSystemResourceAccessor.class).fine("File '"+path+"' does not exist");
        }
        return null;
    }
}

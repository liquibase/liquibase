package liquibase.resource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * {@link PathHandler} that converts the path into a {@link DirectoryResourceAccessor}.
 */
public class ZipPathHandler extends AbstractPathHandler {

    /**
     * Returns {@link #PRIORITY_SPECIALIZED} for all "jar:file:" or files that end in ".jar" or ".zip"
     */
    public int getPriority(String root) {
        if (root == null) {
            return PRIORITY_NOT_APPLICABLE;
        }

        if (root.toLowerCase().endsWith(".zip") || root.toLowerCase().endsWith(".jar")) {
            return PRIORITY_SPECIALIZED;
        }

        return PRIORITY_NOT_APPLICABLE;
    }

    public ResourceAccessor getResourceAccessor(String root) throws FileNotFoundException {
        root = root.replace("jar:", "");

        if (root.matches("^\\w\\w+:.*")) {
            return new ZipResourceAccessor(Paths.get(URI.create(root)));
        }
        return new ZipResourceAccessor(new File(root));
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
        return Files.newOutputStream(Paths.get(path), StandardOpenOption.CREATE_NEW);
    }

    @Override
    public boolean isAbsolute(String path) throws IOException {
        return false;
    }
}

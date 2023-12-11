package liquibase.resource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * {@link PathHandler} that converts the path into a {@link DirectoryResourceAccessor}.
 */
public class ZipPathHandler extends AbstractPathHandler {

    /**
     * Returns {@link #PRIORITY_SPECIALIZED} for all "jar:file:" or files that end in ".jar" or ".zip"
     */
    @Override
    public int getPriority(String root) {
        if (root == null) {
            return PRIORITY_NOT_APPLICABLE;
        }

        if (root.toLowerCase().endsWith(".zip") || root.toLowerCase().endsWith(".jar")) {
            return PRIORITY_SPECIALIZED;
        }

        if (root.startsWith("jar:") && root.endsWith("!/")) { //only can handle `jar:` urls for the entire jar
            return PRIORITY_SPECIALIZED;
        }

        return PRIORITY_NOT_APPLICABLE;
    }

    @Override
    public ResourceAccessor getResourceAccessor(String root) throws FileNotFoundException {
        root = root.replace("jar:", "").replace("!/", "");

        if (root.matches("^\\w\\w+:.*")) {
            return new ZipResourceAccessor(Paths.get(URI.create(root)));
        }
        return new ZipResourceAccessor(new File(root));
    }

    @Override
    public Resource getResource(String path) throws IOException {
        return new PathResource(path, Paths.get(path));
    }

    @Override
    public OutputStream createResource(String path) throws IOException {
        return Files.newOutputStream(Paths.get(path), StandardOpenOption.CREATE_NEW);
    }
}

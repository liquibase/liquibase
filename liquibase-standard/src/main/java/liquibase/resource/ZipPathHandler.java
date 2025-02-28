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
import java.util.Arrays;

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
        int startIndex = root.startsWith("jar:") ? 4 : 0;
        int endIndex = root.lastIndexOf('!');
        root = root.substring(startIndex, endIndex > 4 ? endIndex : root.length());

        if (root.matches("^\\w\\w+:.*")) {
            String[] paths = root.split("!");

            Path rootPath = Paths.get(URI.create(paths[0]));

            // check for embedded jar files
            if (paths.length > 1) {
                return new ZipResourceAccessor(rootPath, Arrays.copyOfRange(paths, 1, paths.length));
            }
            else {
                return new ZipResourceAccessor(rootPath);
            }
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

package liquibase.resource;

import liquibase.Scope;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.util.CollectionUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;

/**
 * A @{link ResourceAccessor} implementation for files on the file system.
 * Will look for files in zip and jar files if they are added as root paths.
 *
 * @deprecated Use {@link DirectoryResourceAccessor} or {@link ZipResourceAccessor}
 */
@Deprecated
public class FileSystemResourceAccessor extends CompositeResourceAccessor {

    /**
     * Creates a FileSystemResourceAccessor with the given directories/files as the roots.
     */
    public FileSystemResourceAccessor(File... baseDirsAndFiles) {
        for (File base : CollectionUtil.createIfNull(baseDirsAndFiles)) {
            addRootPath(base);
        }
    }

    /**
     * @deprecated use {@link FileSystemResourceAccessor#FileSystemResourceAccessor(File...)}
     */
    public FileSystemResourceAccessor(String file) {
        this(new File(file));

    }

    protected void addRootPath(Path path) {
        if (path == null) {
            return;
        }
        addRootPath(path.toFile());
    }

    protected void addRootPath(File base) {
        if (base == null) {
            return;
        }

        try {
            if (!base.exists()) {
                Scope.getCurrentScope().getLog(getClass()).warning("Non-existent path: " + base.getAbsolutePath());
            } else if (base.isDirectory()) {
                addResourceAccessor(new DirectoryResourceAccessor(base));
            } else if (base.getName().endsWith(".jar") || base.getName().toLowerCase().endsWith("zip")) {
                addResourceAccessor(new ZipResourceAccessor(base));
            } else {
                throw new IllegalArgumentException(base.getAbsolutePath() + " must be a directory, jar or zip");
            }
        } catch (FileNotFoundException e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }
}

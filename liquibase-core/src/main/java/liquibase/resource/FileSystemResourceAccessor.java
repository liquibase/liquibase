package liquibase.resource;

import liquibase.util.Validate;

import java.io.File;
import java.net.URLClassLoader;

/**
 * A @{link ResourceAccessor} implementation which finds Files in the File System.
 */
public class FileSystemResourceAccessor extends ClassLoaderResourceAccessor {

    /**
     * Uses current working directory to locate relative files.
     */
    public FileSystemResourceAccessor() {
        this(System.getProperty("user.dir"));
    }

    /**
     * Uses base to locate relative files.
     */
    public FileSystemResourceAccessor(String base) {
        this(new File(base));
    }

    /**
     * Uses base to locate relative files.
     */
    public FileSystemResourceAccessor(File base) {
        super(createURLClassLoader(Validate.notNullArgument(base, "baseDirectory is required.")));
        Validate.isTrueArgument(base.isDirectory(), base + " must be a directory");
    }

    @Override
    public String toString() {
        URLClassLoader cl = URLClassLoader.class.cast(classLoader);
        return getClass().getName() + "(" + cl.getURLs()[0].toExternalForm() + ")";
    }
}

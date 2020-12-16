package liquibase.resource;

import java.io.IOException;
import java.nio.file.OpenOption;
import java.nio.file.Path;

/**
 * Standardized interface for interacting writing files.
 * The {@link ResourceAccessor} is a read-only interface, where files may be scattered across multiple locations and combined into one view.
 * For writing files, there needs to be more direct control over exactly where files are being written to.
 * <br><br>
 * This interface works through the {@link java.nio.file.Path} interface to provide an abstraction around the underlying storage.
 * Use standard path functions like {@link java.nio.file.Files#newOutputStream(Path, OpenOption...)} etc.to work with the path objects returned.
 * <br><br>
 * Implementations of this interface may define ways to specify "root" directories that they work from for relative paths.
 * <br><br>
 * <b>NOTE: while you are able to read from the Paths returned by this interface, you should usually be using the {@link ResourceAccessor} for all file reading.</b>
 * The only time you would want to read from this interface is if you think there may be a difference between what is configured in the ResourceAccessor vs. the file you are going to write to.
 */
public interface ResourceWriter {

    /**
     * Return a {@link java.nio.file.Path} object for the given filePath. If filePath is relative, it is up to the implementation to determine what it is relative to.
     * It is up to the implementation to decide if they wish to support absolute paths or not.
     *
     * @throws IOException if the given filePath is invalid or cannot be converted to a path.
     */
    Path getPath(String filePath) throws IOException;

}

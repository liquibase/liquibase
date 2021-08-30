package liquibase.util.file;

/**
 * @deprecated use {@link liquibase.util.FilenameUtils}
 */
public class FilenameUtils extends liquibase.util.FilenameUtils {

    /**
     * @deprecated use {@link #getDirectory(String)}
     */
    public static String getFullPath(String filename) {
        return getDirectory(filename);
    }
}

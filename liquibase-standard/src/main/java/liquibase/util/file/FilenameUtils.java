package liquibase.util.file;

import liquibase.util.FilenameUtil;

/**
 * @deprecated use {@link FilenameUtil}
 */
@Deprecated
public class FilenameUtils extends FilenameUtil {

    /**
     * @deprecated use {@link #getDirectory(String)}
     */
    @Deprecated
    public static String getFullPath(String filename) {
        return getDirectory(filename);
    }
}

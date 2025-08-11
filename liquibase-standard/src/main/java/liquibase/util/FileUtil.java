package liquibase.util;

import liquibase.GlobalConfiguration;
import liquibase.Scope;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class FileUtil {
    
    private FileUtil() {
        throw new IllegalStateException("This utility class must not be instantiated. Sorry.");
    }

    public static String getContents(File file) throws IOException {
        if (!file.exists()) {
            return null;
        }
        return FileUtils.readFileToString(file);
    }

    public static void write(String contents, File file) throws IOException {
        write(contents, file, false);
    }

    /**
     * @deprecated use {@link FileUtils#write(File, CharSequence, String, boolean)}
     */
    @Deprecated
    public static void write(String contents, File file, boolean append) throws IOException {
        FileUtils.write(file, contents, GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue(), append);
    }

    public static String getFileNotFoundMessage(String physicalChangeLogLocation) {
        //
        // Check for any prefix which is not file:
        // Take this to indicate a full path
        //
        if (physicalChangeLogLocation.matches("^\\w\\w+:.*") && ! physicalChangeLogLocation.startsWith("file:")) {
            return "ERROR: The file '" + physicalChangeLogLocation + "' was not found." + System.lineSeparator() +
                    "The file property cannot be configured with a fully qualified path, but must be a relative path on the property," + System.lineSeparator() +
                    "and any local or remote base of the path set on the search path.";
        }
        String message = "The file " + physicalChangeLogLocation + " was not found in the configured search path:" + System.lineSeparator();
        StringBuilder builder = new StringBuilder(message);
        for (String location : Scope.getCurrentScope().getResourceAccessor().describeLocations()) {
             builder.append("    - ").append(location).append(System.lineSeparator());
        }
        builder.append("More locations can be added with the 'searchPath' parameter.");

        return builder.toString();
    }

    public static boolean isAbsolute(String path) {
        if (path == null) {
            return false;
        }
        return new File(path).isAbsolute();
    }
}

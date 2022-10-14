package liquibase.util;

import liquibase.Scope;
import liquibase.GlobalConfiguration;

import java.io.*;

public class FileUtil {
    
    private FileUtil() {
        throw new IllegalStateException("This utility class must not be instantiated. Sorry.");
    }

    public static String getContents(File file) throws IOException {
        if (!file.exists()) {
            return null;
        }
        try (
            FileInputStream fileInputStream = new FileInputStream(file);
        ) {
            
            return StreamUtil.readStreamAsString(fileInputStream);
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    public static void write(String contents, File file) throws IOException {
        write(contents, file, false);
    }

    public static void write(String contents, File file, boolean append) throws IOException {
        if (file.getParentFile() != null) {
            file.getParentFile().mkdirs();
        }

        try (
                FileOutputStream output = new FileOutputStream(file, append)
        ){
            StreamUtil.copy(new ByteArrayInputStream(contents.getBytes(GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue())), output);
        }

    }

    public static String getFileNotFoundMessage(String physicalChangeLogLocation) {
        String message = "The file " + physicalChangeLogLocation + " was not found in the configured search path:" + System.lineSeparator();
        for (String location : Scope.getCurrentScope().getResourceAccessor().describeLocations()) {
            message += "    - " + location + System.lineSeparator();
        }
        message += "More locations can be added with the 'searchPath' parameter.";

        return message;
    }

    public static boolean isAbsolute(String path) {
        if (path == null) {
            return false;
        }
        return new File(path).isAbsolute();
    }
}

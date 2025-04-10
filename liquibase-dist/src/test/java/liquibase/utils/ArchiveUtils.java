package utils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ArchiveUtils {
    public static String getGeneratedArchivePath(String directory, String prefix, String suffix) throws IOException {
        File dir = new File(directory);
        File[] files = dir.listFiles();
        if (files == null) {
            throw new IOException("No files found in directory: " + directory);
        }
        for (File file : files) {
            if (file.getName().startsWith(prefix) && file.getName().endsWith(suffix)) {
                return file.getCanonicalPath();
            }
        }
        throw new IOException("No matching archive found in directory: " + directory);
    }

    public static String getSortedLines(String expected) {
        return Arrays.stream(expected.split("\n")).sorted().collect(Collectors.joining("\n"));
    }
}

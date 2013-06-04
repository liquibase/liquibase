package liquibase.util;

import java.io.File;
import java.io.IOException;

public class FileUtil {
    /**
     * Schedule a file to be deleted when JVM exits.
     * If file is directory delete it and all sub-directories.
     */
    public static void forceDeleteOnExit( final File file ) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    FileUtil.deleteDirectory(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Recursively schedule directory for deletion on JVM exit.
     */
    private static void deleteDirectory( final File directory ) throws IOException {
        if ( !directory.exists() ) {
            return;
        }

        cleanDirectory(directory);
        if (!directory.delete()) {
            throw new IOException("Cannot delete "+directory.getAbsolutePath());
        }
    }

    /**
     * Clean a directory without deleting it.
     */
    private static void cleanDirectory(final File directory) throws IOException {
        if ( !directory.exists() ) {
            return;
        }

        if ( !directory.isDirectory() ) {
            return;
        }

        IOException exception = null;

        final File[] files = directory.listFiles();
        if (files != null) {
            for (final File file : files) {
                try {
                    cleanDirectory(file);
                    if (!file.delete()) {
                        throw new IOException("Cannot delete "+file.getAbsolutePath());
                    }
                } catch (final IOException ioe) {
                    exception = ioe;
                }
            }
        }

        if ( null != exception ) {
            throw exception;
        }
    }

}

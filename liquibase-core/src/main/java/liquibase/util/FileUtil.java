package liquibase.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

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

    public static File extractZipFile(URL resource) throws IOException {
        String file = resource.getFile();
        String path = file.split("!")[0];
        if (path.matches("file:\\/[A-Za-z]:\\/.*")) {
            path = path.replaceFirst("file:\\/", "");
        } else {
            path = path.replaceFirst("file:", "");
        }
        path = URLDecoder.decode(path, "UTF-8");
        File zipfile = new File(path);

        File tempDir = File.createTempFile("liquibase-unzip", ".dir");
        tempDir.delete();
        tempDir.mkdir();

        JarFile jarFile = new JarFile(zipfile);
        try {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                File entryFile = new File(tempDir, entry.getName());
                entryFile.mkdirs();
            }

            FileUtil.forceDeleteOnExit(tempDir);
        } finally {
            jarFile.close();
        }

        return tempDir;
    }
}

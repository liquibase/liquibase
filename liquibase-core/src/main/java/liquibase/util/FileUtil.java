package liquibase.util;

import java.io.*;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class FileUtil {

    /**
     * Schedule a file to be deleted when JVM exits.
     * If file is directory delete it and all sub-directories.
     */
    public static void deleteOnExit(final File file) {
        file.deleteOnExit();
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File child  : files) {
                    deleteOnExit(child);
                }
            }
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

    /**
     * Unzips the given zip file and returns a File object corresponding to the root directory.
     * The returned directory is a temporary directory that will be deleted on application exit.
     */
    public static File unzip(File zipFile) throws IOException {
        File tempDir = File.createTempFile("liquibase-unzip", ".dir");
        tempDir.delete();
        tempDir.mkdir();

        JarFile jarFile = new JarFile(zipFile);
        try {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                File entryFile = new File(tempDir, entry.getName());
                if (!entry.isDirectory()) {
                    entryFile.getParentFile().mkdirs();
                    FileOutputStream out = new FileOutputStream(entryFile);

                    byte[] buf = new byte[1024];
                    int len;
                    InputStream inputStream = jarFile.getInputStream(entry);
                    while ((len = inputStream.read(buf)) > 0) {
                        if (!zipFile.exists()) {
                            zipFile.getParentFile().mkdirs();
                        }
                        out.write(buf, 0, len);
                    }
                    inputStream.close();
                    out.close();
                }
            }

            FileUtil.deleteOnExit(tempDir);
        } finally {
            jarFile.close();
        }

        return tempDir;
    }

    public static String getContents(File file) throws IOException {
        if (!file.exists()) {
            return null;
        }
        FileReader reader = null;
        try {
            reader = new FileReader(file);
            return StreamUtil.getReaderContents(reader);
        } catch (FileNotFoundException e) {
            return null;
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    public static void write(String contents, File file) throws IOException {
        file.getParentFile().mkdirs();
        FileOutputStream output = new FileOutputStream(file);
        try {
            StreamUtil.copy(new ByteArrayInputStream(contents.getBytes("UTF-8")), output);
        } finally {
            output.close();
        }
    }
}

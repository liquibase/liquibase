package liquibase.util;

import liquibase.configuration.GlobalConfiguration;
import liquibase.configuration.LiquibaseConfiguration;

import java.io.*;

public class FileUtil {

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

    public static String getContents(File file) throws IOException {
        if (!file.exists()) {
            return null;
        }
        Reader reader = null;
        try {
            reader = new InputStreamReader(new FileInputStream(file), LiquibaseConfiguration.getInstance().getConfiguration(GlobalConfiguration.class).getOutputEncoding());
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
            StreamUtil.copy(new ByteArrayInputStream(contents.getBytes(LiquibaseConfiguration.getInstance().getConfiguration(GlobalConfiguration.class).getOutputEncoding())), output);
        } finally {
            output.close();
        }
    }
}

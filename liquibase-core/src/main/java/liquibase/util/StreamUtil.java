package liquibase.util;

import java.io.*;
import java.nio.charset.Charset;

import liquibase.changelog.ChangeSet;
import liquibase.resource.ResourceAccessor;
import liquibase.resource.UtfBomAwareReader;

/**
 * Utilities for working with streams.
 */
public class StreamUtil {
	
	final public static String lineSeparator = System.getProperty("line.separator");
	
    public static String getLineSeparator() {
        return lineSeparator;
    }

    /**
     * Reads a stream until the end of file into a String and uses the machines
     * default encoding to convert to characters the bytes from the Stream.
     *
     * @param ins The InputStream to read.
     * @return The contents of the input stream as a String
     * @throws IOException If there is an error reading the stream.
     */
    public static String getStreamContents(InputStream ins) throws IOException {
        return getReaderContents(new UtfBomAwareReader(ins));
    }

    /**
     * Reads a stream until the end of file into a String and uses the machines
     * default encoding to convert to characters the bytes from the Stream.
     *
     * @param ins The InputStream to read.
     * @param  charsetName The name of a supported {@link java.nio.charset.Charset </code>charset<code>}
     * @return The contents of the input stream as a String
     * @throws IOException If there is an error reading the stream.
     */
	public static String getStreamContents(InputStream ins, String charsetName)
			throws IOException {
		UtfBomAwareReader reader;

		if (charsetName == null) {
			reader = new UtfBomAwareReader(ins);
		} else {
			String charsetCanonicalName = Charset.forName(charsetName).name();
			String encoding;

			reader = new UtfBomAwareReader(ins, charsetName);
			encoding = Charset.forName(reader.getEncoding()).name();

			if (charsetCanonicalName.startsWith("UTF")
					&& !charsetCanonicalName.equals(encoding)) {
				reader.close();
				throw new IllegalArgumentException("Expected encoding was '"
						+ charsetCanonicalName + "' but a BOM was detected for '"
						+ encoding + "'");
			}
		}
		return getReaderContents(reader);
	}
    
    /**
     * Reads all the characters into a String.
     *
     * @param reader The Reader to read.
     * @return The contents of the input stream as a String
     * @throws IOException If there is an error reading the stream.
     */
    public static String getReaderContents(Reader reader) throws IOException {
        try {
            StringBuffer result = new StringBuffer();

            char[] buffer = new char[2048];
            int read;
            while ((read = reader.read(buffer)) > -1) {
                result.append(buffer, 0, read);
            }
            return result.toString();
        } finally {
            closeQuietly(reader);
        }
    }

    public static void copy(InputStream inputStream, OutputStream outputStream) throws IOException {
        byte[] bytes = new byte[1024];
        int r = inputStream.read(bytes);
        while (r > 0) {
            outputStream.write(bytes, 0, r);
            r = inputStream.read(bytes);
        }
    }

    public static long getContentLength(InputStream in) throws IOException
    {
        long length = 0;
        byte[] buf = new byte[4096];
        int bytesRead = in.read(buf);
        while (bytesRead > 0) {
            length += bytesRead;
            bytesRead = in.read(buf);
        }
        return length;
    }

    public static long getContentLength(Reader reader) throws IOException
    {
        long length = 0;
        char[] buf = new char[2048];
        int charsRead = reader.read(buf);
        while (charsRead > 0) {
            length += charsRead;
            charsRead = reader.read(buf);
        }
        return length;
    }
    
    public static void closeQuietly(Reader input) {
        closeQuietly((Closeable) input);
    }
    
    public static void closeQuietly(InputStream input) {
        closeQuietly((Closeable) input);
    }
    
    public static void closeQuietly(Closeable input) {
        try {
            if (input != null) {
                input.close();
            }
        } catch (IOException ioe) {
            // ignore
        }
    }

    public static InputStream openStream(String path, Boolean relativeToChangelogFile, ChangeSet changeSet, ResourceAccessor resourceAccessor) throws IOException {
        InputStream stream = openFromClasspath(path, relativeToChangelogFile, changeSet, resourceAccessor);
        if (stream == null) {
            stream = openFromFileSystem(path, relativeToChangelogFile, changeSet, resourceAccessor);
        }

        return stream;
    }

    /**
     * Tries to load the file from the file system.
     *
     * @param file The name of the file to search for
     * @return True if the file was found, false otherwise.
     */
    private static InputStream openFromFileSystem(String file, Boolean relativeToChangelogFile, ChangeSet changeSet, ResourceAccessor resourceAccessor) throws IOException {
        if (resourceAccessor == null) {
            return null;
        }
        if (relativeToChangelogFile != null && relativeToChangelogFile) {
            String base;
            if (changeSet.getChangeLog() == null) {
                base = changeSet.getFilePath();
            } else {
                base = changeSet.getChangeLog().getPhysicalFilePath().replaceAll("\\\\","/");
            }
            if (!base.contains("/")) {
                base = ".";
            }
            file = base.replaceFirst("/[^/]*$", "") + "/" + file;
        }

        try {
            return resourceAccessor.getResourceAsStream(file);
        } catch (FileNotFoundException fnfe) {
            return null;
        }

    }

    /**
     * Tries to load a file using the FileOpener.
     * <p/>
     * If the fileOpener can not be found then the attempt to load from the
     * classpath the return is false.
     *
     * @param file The file name to try and find.
     * @return True if the file was found and loaded, false otherwise.
     */
    private static InputStream openFromClasspath(String file, Boolean relativeToChangelogFile, ChangeSet changeSet, ResourceAccessor resourceAccessor) throws IOException {
        if (resourceAccessor == null) {
            return null;
        }

        if (relativeToChangelogFile != null && relativeToChangelogFile) {
            String base;
            if (changeSet.getChangeLog() == null) {
                base = changeSet.getFilePath();
            } else {
                base = changeSet.getChangeLog().getPhysicalFilePath().replaceAll("\\\\","/");
            }
            if (!base.contains("/")) {
                base = ".";
            }

            file = base.replaceFirst("/[^/]*$", "") + "/" + file;
        }

        return resourceAccessor.getResourceAsStream(file);
    }
}

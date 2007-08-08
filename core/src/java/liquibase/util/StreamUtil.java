package liquibase.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Utilities for working with streams.
 */
public class StreamUtil {

    public static String getLineSeparator() {
        return System.getProperty("line.separator");
    }

    /**
     * Reads a stream until the end of file into a String and uses the machines
     * default encoding to convert to characters the bytes from the Stream.
     * 
     * @param ins
     *            The InputStream to read.
     * @return The contents of the input stream as a String
     * @throws IOException
     *             If there is an error reading the stream.
     */
    public static String getStreamContents(InputStream ins) throws IOException {

        InputStreamReader reader = new InputStreamReader(ins);
        return getReaderContents(reader);
    }

    /**
     * Reads all the characters into a String.
     * 
     * @param reader
     *            The Reader to read.
     * @return The contents of the input stream as a String
     * @throws IOException
     *             If there is an error reading the stream.
     */
    public static String getReaderContents(Reader reader) throws IOException {
        try {
            StringBuffer result = new StringBuffer();

            char[] buffer = new char[2048];
            int read = 0;
            while ((read = reader.read(buffer)) > -1) {
                result.append(buffer, 0, read);
            }
            return result.toString();
        } finally {
            try {
                reader.close();
            } catch (IOException ioe) {
                // can safely ignore
            }
        }
    }
}

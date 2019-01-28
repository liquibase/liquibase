package liquibase.util;

import liquibase.Scope;
import liquibase.configuration.GlobalConfiguration;
import liquibase.configuration.LiquibaseConfiguration;

import java.io.*;
import java.nio.charset.Charset;

/**
 * Utilities for working with streams.
 */
public class StreamUtil {

    public static String getLineSeparator() {
        return LiquibaseConfiguration.getInstance().getConfiguration(GlobalConfiguration.class).getOutputLineSeparator();
    }

    public static void copy(InputStream inputStream, OutputStream outputStream) throws IOException {
        byte[] bytes = new byte[1024];
        int r = inputStream.read(bytes);
        while (r > 0) {
            outputStream.write(bytes, 0, r);
            r = inputStream.read(bytes);
        }
    }

    public static byte[] readStream(InputStream stream) throws IOException {
        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {

            copy(stream, buffer);
            buffer.flush();

            return buffer.toByteArray();
        }
    }

    /**
     * Calls {@link #readStreamAsString(InputStream, String)} with {@link Scope#getFileEncoding()} as the encoding
     */
    public static String readStreamAsString(InputStream stream) throws IOException {
        return readStreamAsString(stream, null);
    }

    /**
     * Returns the given stream as a string using the given encoding.
     * If encoding is null, use {@link Scope#getFileEncoding()}
     */
    public static String readStreamAsString(InputStream stream, String encoding) throws IOException {
        StringBuilder result = new StringBuilder();

        try (Reader reader = readStreamWithReader(stream, encoding)) {

            char[] buffer = new char[2048];
            int read;
            while ((read = reader.read(buffer)) > -1) {
                result.append(buffer, 0, read);
            }
            return result.toString();
        }
    }

    public static Reader readStreamWithReader(InputStream stream, String encoding) throws IOException {
        BomAwareInputStream encodingAwareStream = new BomAwareInputStream(stream);
        Charset detectedEncoding = encodingAwareStream.getDetectedCharset();

        if (encoding == null) {
            if (detectedEncoding != null) {
                encoding = detectedEncoding.name();
            }
        } else {
            String canonicalEncodingName = Charset.forName(encoding).name();

            if (detectedEncoding != null && canonicalEncodingName.startsWith("UTF") && !canonicalEncodingName.equals(detectedEncoding.name())) {
                throw new IllegalArgumentException("Expected encoding was '"
                        + encoding + "' but a BOM was detected for '"
                        + detectedEncoding + "'");
            }
        }

        return new InputStreamReader(encodingAwareStream, ObjectUtil.defaultIfNull(encoding, Scope.getCurrentScope().getFileEncoding().toString()));
    }

}
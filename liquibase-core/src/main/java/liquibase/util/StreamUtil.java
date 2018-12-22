package liquibase.util;

import liquibase.Scope;
import liquibase.changelog.ChangeSet;
import liquibase.configuration.GlobalConfiguration;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.exception.LiquibaseException;
import liquibase.logging.LogService;
import liquibase.logging.LogType;
import liquibase.resource.ResourceAccessor;
import liquibase.resource.UtfBomAwareReader;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Set;

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
        try(ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {

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
        return new String(readStream(stream), ObjectUtil.defaultIfNull(encoding, Scope.getCurrentScope().getFileEncoding().toString()));
    }

}

package liquibase.util;

import liquibase.GlobalConfiguration;
import liquibase.changelog.ChangeSet;
import liquibase.resource.ResourceAccessor;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.Charset;

/**
 * Utilities for working with streams.
 */
public abstract class StreamUtil {

    public static String getLineSeparator() {
        return GlobalConfiguration.OUTPUT_LINE_SEPARATOR.getCurrentValue();
    }

    /**
     * @deprecated use {@link IOUtils#copy(InputStream, OutputStream)}
     */
    @Deprecated
    public static void copy(InputStream inputStream, OutputStream outputStream) throws IOException {
        IOUtils.copy(inputStream, outputStream);
    }

    /**
     * @deprecated use {@link IOUtils#toByteArray(InputStream)}
     */
    @Deprecated
    public static byte[] readStream(InputStream stream) throws IOException {
        return IOUtils.toByteArray(stream);
    }

    /**
     * Calls {@link #readStreamAsString(InputStream, String)} with {@link GlobalConfiguration#FILE_ENCODING} as the encoding
     */
    public static String readStreamAsString(InputStream stream) throws IOException {
        return readStreamAsString(stream, null);
    }

    /**
     * Returns the given stream as a string using the given encoding.
     * If encoding is null, use {@link GlobalConfiguration#FILE_ENCODING}
     */
    public static String readStreamAsString(InputStream stream, String encoding) throws IOException {
        try (Reader reader = readStreamWithReader(stream, encoding)) {
            return IOUtils.toString(reader);
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

        return new InputStreamReader(encodingAwareStream, ObjectUtil.defaultIfNull(encoding == null ? null : Charset.forName(encoding), GlobalConfiguration.FILE_ENCODING.getCurrentValue()));
    }

    /**
     * @deprecated use {@link ResourceAccessor#openStream(String, String)}
     */
    @Deprecated
    public static InputStream openStream(String path, Boolean relativeToChangelogFile, ChangeSet changeSet, ResourceAccessor resourceAccessor) throws IOException {
        if (relativeToChangelogFile != null && relativeToChangelogFile) {
            path = resourceAccessor.get(changeSet.getChangeLog().getPhysicalFilePath()).resolveSibling(path).getPath();
        }
        return resourceAccessor.getExisting(path).openInputStream();
    }
}

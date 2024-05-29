package liquibase.util;

import lombok.Getter;
import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * Read up to 4 bytes to determine the BOM. Extra bytes, of if no BOM is
 * found are pushed back to the input stream. If no BOM is found, the
 * detectedCharsetName is null.
 * @deprecated use {@link BOMInputStream} instead
 */
@Deprecated
public class BomAwareInputStream extends BOMInputStream {

    /**
     * Returns detected charset name. Null if no BOM header was found.
     * @return charset name - one of UTF-8, UTF-16BE, UTF-32LE, UTF-16LE, UTF-32BE or null if no BOM detected
     */
    @Getter
    private Charset detectedCharset;

    public BomAwareInputStream(InputStream in) throws IOException {
        super(in, false, ByteOrderMark.UTF_8, ByteOrderMark.UTF_16BE, ByteOrderMark.UTF_32LE, ByteOrderMark.UTF_16LE, ByteOrderMark.UTF_32BE);
        init();
    }

    protected void init() throws IOException {
        String bomCharsetName = getBOMCharsetName();
        if (bomCharsetName != null) {
            detectedCharset = Charset.forName(bomCharsetName);
        }
    }
}

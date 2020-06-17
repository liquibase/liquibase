package liquibase.resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

/**
 * Read up to 4 bytes to determine the BOM. Extra bytes, of if no BOM is
 * found are pushed back to the input stream. If no BOM is found, the
 * detectedCharsetName is null.
 *
 * @author Dominique Broeglin
 * @author Vity
 */
public class UtfBomStripperInputStream extends PushbackInputStream {
	private static final byte _0xBF = (byte) 0xBF;
	private static final byte _0x00 = (byte) 0x00;
	private static final byte _0xBB = (byte) 0xBB;
	private static final byte _0xFF = (byte) 0xFF;
	private static final byte _0xFE = (byte) 0xFE;
	private static final byte _0xEF = (byte) 0xEF;

    private String detectedCharsetName;

	public UtfBomStripperInputStream(InputStream in) throws IOException {
        super(in, 4);
        init();
	}

    /**
     * Returns detected charset name. Null if no BOM header was found.
     * @return charset name - one of UTF-8, UTF-16BE, UTF-32LE, UTF-16LE, UTF-32BE or null if no BOM detected
     */
    public String getDetectedCharsetName() {
        return detectedCharsetName;
    }

	protected void init() throws IOException {
		byte bom[] = new byte[4];
		int n, unread;
		n = this.read(bom, 0, bom.length);

		if ((bom[0] == _0xEF) && (bom[1] == _0xBB) && (bom[2] == _0xBF)) {
            detectedCharsetName = "UTF-8";
			unread = n - 3;
		} else if ((bom[0] == _0xFE) && (bom[1] == _0xFF)) {
            detectedCharsetName = "UTF-16BE";
			unread = n - 2;
		} else if ((bom[0] == _0xFF) && (bom[1] == _0xFE)) {
			if ((n == 4) && (bom[2] == _0x00) && (bom[3] == _0x00)) {
                detectedCharsetName = "UTF-32LE";
				unread = 0;
			} else {
                detectedCharsetName = "UTF-16LE";
				unread = n - 2;
			}
		} else if ((bom[0] == _0x00) && (bom[1] == _0x00) && (bom[2] == _0xFE) && (bom[3] == _0xFF)) {
            detectedCharsetName = "UTF-32BE";
			unread = 4;
		} else {
			unread = n;
		}

		if (unread > 0) {
			this.unread(bom, (n - unread), unread);
		} else if (unread < -1) {
			this.unread(bom, 0, 0);
		}
	}

}

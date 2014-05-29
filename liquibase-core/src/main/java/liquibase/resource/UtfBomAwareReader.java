package liquibase.resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.io.Reader;
import java.nio.charset.Charset;

/**
 * Reader that tries to identify the encoding by looking at the BOM. If no BOM
 * is found it defaults to the encoding givent at initialization. Original ideas
 * from Thomas Weidenfeller, Aki Nieminen.
 * 
 * @author Dominique Broeglin
 */
public class UtfBomAwareReader extends Reader {
	private static final byte _0xBF = (byte) 0xBF;
	private static final byte _0x00 = (byte) 0x00;
	private static final byte _0xBB = (byte) 0xBB;
	private static final byte _0xFF = (byte) 0xFF;
	private static final byte _0xFE = (byte) 0xFE;
	private static final byte _0xEF = (byte) 0xEF;

	private PushbackInputStream pis;
	private InputStreamReader is = null;
	private String defaultCharsetName;

	public UtfBomAwareReader(InputStream in) {
		pis = new PushbackInputStream(in, 4);
		this.defaultCharsetName = "UTF-8";
	}
	
	public UtfBomAwareReader(InputStream in, String defaultCharsetName) {
		pis = new PushbackInputStream(in, 4);
		if (defaultCharsetName == null)
			throw new NullPointerException("defaultCharsetName");
		this.defaultCharsetName = defaultCharsetName;
	}

	public String getDefaultEncoding() {
		return defaultCharsetName;
	}

	public String getEncoding() {
		if (is == null) {
			try {
				init();
			} catch (IOException e) {
				throw new IllegalStateException("Unable to determine encoding",
						e);
			}
		}
		return is.getEncoding();
	}

	/**
	 * Read up to 4 bytes to determine the BOM. Extra bytes, of if no BOM is
	 * found are pushed back to the input stream. If no BOM is found, the
	 * defaultCharsetName is used to initialize the reader.
	 */
	protected void init() throws IOException {
		String charsetName;

		byte bom[] = new byte[4];
		int n, unread;
		n = pis.read(bom, 0, bom.length);

		if (bom[0] == _0xEF && bom[1] == _0xBB && bom[2] == _0xBF) {
			charsetName = "UTF-8";
			unread = n - 3;
		} else if (bom[0] == _0xFE && bom[1] == _0xFF) {
			charsetName = "UTF-16BE";
			unread = n - 2;
		} else if (bom[0] == _0xFF && bom[1] == _0xFE) {
			if (n == 4 && bom[2] == _0x00 && bom[3] == _0x00) {
				charsetName = "UTF-32LE";
				unread = 0;
			} else {
				charsetName = "UTF-16LE";
				unread = n - 2;
			}
		} else if (bom[0] == _0x00 && bom[1] == _0x00 && bom[2] == _0xFE
				&& bom[3] == _0xFF) {
			charsetName = "UTF-32BE";
			unread = 4;
		} else {
			charsetName = defaultCharsetName;
			unread = n;
		}

		if (unread > 0) {
			pis.unread(bom, (n - unread), unread);
		} else if (unread < -1) {
			pis.unread(bom, 0, 0);
		}

		if (charsetName == null) {
			is = new InputStreamReader(pis);
		} else {
			is = new InputStreamReader(pis, charsetName);
		}
	}

	@Override
    public void close() throws IOException {
		if (is == null) {
			init();
		}
		is.close();
	}

	@Override
    public int read(char[] cbuf, int off, int len) throws IOException {
		if (is == null) {
			init();
		}
		return is.read(cbuf, off, len);
	}
}

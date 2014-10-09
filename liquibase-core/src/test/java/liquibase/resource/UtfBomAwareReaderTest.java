package liquibase.resource;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.junit.Ignore;
import org.junit.Test;

public class UtfBomAwareReaderTest {

	private InputStream prepareStream(int... bytes) {
		byte[] buffer = new byte[bytes.length];

		for (int i = 0; i < bytes.length; i++) {
			buffer[i] = (byte) bytes[i];
		}
		return new ByteArrayInputStream(buffer);
	}

	UtfBomAwareReader reader;

	private void prepare(int... buffer) {
		InputStream is = prepareStream(buffer);

		reader = new UtfBomAwareReader(is, "ISO8859_1");
	}

	@Test
	public void testEmpty() throws IOException {
		prepare();
		assertEncoding("ISO-8859-1");
	}

	@Test
	public void testNoBom() throws IOException {
		prepare(0x61, 0x62, 0x63);
		assertEncoding("ISO-8859-1");
	}

	@Test
	public void testUtf8Empty() throws IOException {
		prepare(0xEF, 0xBB, 0xBF);
		assertEncoding("UTF-8");
		assertEmpty();
	}

	@Test
	public void testEmptyUtf8WithFourBytesOnly() throws IOException {
		prepare(0xEF, 0xBB, 0xBF, 0x61);
		assertEncoding("UTF-8");
	}

	@Test
	public void testUtf8() throws IOException {
		prepare(0xEF, 0xBB, 0xBF, 0x61, 0x62, 0x63);
		assertEncoding("UTF-8");
		assertData();
	}

	@Test
	public void testUtf16BEEmpty() throws IOException {
		prepare(0xFE, 0xFF);
		assertEncoding("UTF-16BE");
		assertEmpty();
	}

	@Test
	public void testEmptyUtf16BEWithFourBytesOnly() throws IOException {
		prepare(0xFE, 0xFF, 0x00, 0x61);
		assertEncoding("UTF-16BE");
	}

	@Test
	public void testUtf16BE() throws IOException {
		prepare(0xFE, 0xFF, 0x00, 0x61, 0x00, 0x62, 0x00, 0x63);
		assertEncoding("UTF-16BE");
		assertData();
	}

	@Test
	public void testUtf16LEEmpty() throws IOException {
		prepare(0xFF, 0xFE);
		assertEncoding("UTF-16LE");
		assertEmpty();
	}

	@Test
	public void testEmptyUtf16LEWithFourBytesOnly() throws IOException {
		prepare(0xFF, 0xFE, 0x61, 0x00);
		assertEncoding("UTF-16LE");
	}

	@Test
	public void testUtf16LE() throws IOException {
		prepare(0xFF, 0xFE, 0x61, 0x00, 0x62, 0x00, 0x63, 0x00);
		assertEncoding("UTF-16LE");
		assertData();
	}

	@Test
	public void testUtf32LEEmpty() throws IOException {
		prepare(0xFF, 0xFE, 0x00, 0x00);
		assertEncoding("UTF-32LE");
	}

	@Test
	public void testUtf32LE() throws IOException {
		prepare(0xFF, 0xFE, 0x00, 0x00, /**/0x61, 0x00, 0x00, 0x00, 0x62,
				0x00, 0x00, 0x00, 0x63, 0x00, 0x00, 0x00);
		assertEncoding("UTF-32LE");
		assertData();
	}

	@Test
	public void testUtf32BEEmpty() throws IOException {
		prepare(0x00, 0x00, 0xFE, 0xFF);
		assertEncoding("UTF-32BE");
		assertEmpty();
	}

	@Test
	public void testUtf32BE() throws IOException {
		prepare(0x00, 0x00, 0xFE, 0xFF, 0x00, 0x00, 0x00, 0x61, 0x00, 0x00,
				0x00, 0x62, 0x00, 0x00, 0x00, 0x63);
		assertEncoding("UTF-32BE");
		assertData();
	}

	@Test
	public void testWithNoDefault() throws IOException {
		reader = new UtfBomAwareReader(prepareStream(0x00, 0x00, 0xFE, 0xFF,
				0x00, 0x00, 0x00, 0x61, 0x00, 0x00, 0x00, 0x62, 0x00, 0x00,
				0x00, 0x63));
		assertEncoding("UTF-32BE", "UTF-8");
		assertData();
	}
	
	private void assertData() throws IOException {
		assertEquals("abc", new BufferedReader(reader).readLine());
		assertEmpty();
	}

	private void assertEmpty() throws IOException {
		assertEquals("reader is not empty", -1, reader.read());
	}

	private void assertEncoding(String expectedCharsetName) throws IOException {
		assertEncoding(expectedCharsetName, "ISO8859_1");
	}

	private void assertEncoding(String expectedCharsetName, String expectedDefault) throws IOException {
		String canonicalCharsetName = Charset.forName(reader.getEncoding())
				.toString();

		assertEquals(expectedCharsetName, canonicalCharsetName);
		assertEquals(expectedDefault, reader.getDefaultEncoding());
	}
}

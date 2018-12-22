package liquibase.util;

import org.junit.Test;

import java.io.*;

import static org.junit.Assert.*;

public class StreamUtilTest {

	@Test
	public void readStreamAsString() throws IOException {
		byte[] contents = "TEST2".getBytes();
		ByteArrayInputStream stream = new ByteArrayInputStream(contents);
		String result = StreamUtil.readStreamAsString(stream);
		assertEquals("TEST2", result);
	}

	public void testWithBomNoEncodingGiven() throws IOException {
		String contents = "abc";
		ByteArrayInputStream bais = new ByteArrayInputStream(new byte[] {
				(byte) 0xEF, (byte) 0xBB, (byte) 0xBF, 0x61, 0x62, 0x63 });

		assertEquals(contents, StreamUtil.readStreamAsString(bais));
	}

	@Test
	public void testWithBomCorrectEncodingGiven() throws IOException {
		String contents = "abc";
		ByteArrayInputStream bais = new ByteArrayInputStream(new byte[] {
				(byte) 0xEF, (byte) 0xBB, (byte) 0xBF, 0x61, 0x62, 0x63 });

		assertEquals(contents, StreamUtil.readStreamAsString(bais, "UTF8"));
	}

	@Test
	public void testWithoutBomUtf8() throws IOException {
		String contents = "abc";
		ByteArrayInputStream bais = new ByteArrayInputStream(new byte[] { 0x61,
				0x62, 0x63 });

		assertEquals(contents, StreamUtil.readStreamAsString(bais, "UTF8"));
	}

	@Test
	public void testWithoutBomLatin1() throws IOException {
		String contents = "abc";
		ByteArrayInputStream bais = new ByteArrayInputStream(new byte[] { 0x61,
				0x62, 0x63 });

		assertEquals(contents, StreamUtil.readStreamAsString(bais, "Latin1"));
	}
	
	@Test
	public void testWithBomWrongEncodingGiven() throws IOException {
		ByteArrayInputStream bais = new ByteArrayInputStream(new byte[] {
				(byte) 0xEF, (byte) 0xBB, (byte) 0xBF, 0x61, 0x62, 0x63 });

		try {
			StreamUtil.readStreamAsString(bais, "UTF-16BE");
			fail("Should have thrown an IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			assertTrue(e.getMessage().contains("UTF-16BE"));
		}
	}
}

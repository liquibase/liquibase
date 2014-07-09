package liquibase.util;

import org.junit.Test;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.assertEquals;

public class MD5UtilTest {

	private static final String TEST_STRING = "foo";
	private static final String TEST_STRING_MD5_HASH = "acbd18db4cc2f85cedef654fccc4a4d8";

    private static final String TEST_STRING2 = "abc";
    private static final String TEST_STRING2_MD5_HASH = "900150983cd24fb0d6963f7d28e17f72";

    private static final String TEST_STRING3 = "bbb";
    private static final String TEST_STRING3_MD5_HASH = "08f8e0260c64418510cefb2b06eee5cd";

	@Test
	public void testComputeMD5() throws Exception {
		String hash = MD5Util.computeMD5(TEST_STRING);
		assertEquals(TEST_STRING_MD5_HASH, hash);

        String hash2 = MD5Util.computeMD5(TEST_STRING2);
        assertEquals(TEST_STRING2_MD5_HASH, hash2);

        String hash3 = MD5Util.computeMD5(TEST_STRING3);
        assertEquals(TEST_STRING3_MD5_HASH, hash3);
	}

	@Test
	public void testComputeMD5InputStream() {
		ByteArrayInputStream bais = new ByteArrayInputStream(TEST_STRING.getBytes());
		String hexString = MD5Util.computeMD5(bais);
		assertEquals(TEST_STRING_MD5_HASH, hexString);
	}

}

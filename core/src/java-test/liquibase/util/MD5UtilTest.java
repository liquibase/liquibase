package liquibase.util;

import static org.junit.Assert.*;

import org.junit.Test;

import hidden.org.codehaus.plexus.util.StringInputStream;


public class MD5UtilTest {
	@Test
	public void testComputeMD5InputStream() {
		String s = "abc";
		StringInputStream is = new StringInputStream(s);
		String hexString = MD5Util.computeMD5(is);
		assertEquals("90150983cd24fb0d6963f7d28e17f72",hexString);
	}
}

package liquibase.resource;

import org.junit.Test;

import java.io.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Vity
 */
public class UtfBomStripperInputStreamTest {

    private InputStream prepareStream(int... bytes) {
        byte[] buffer = new byte[bytes.length];

        for (int i = 0; i < bytes.length; i++) {
            buffer[i] = (byte) bytes[i];
        }
        return new ByteArrayInputStream(buffer);
    }


    private UtfBomStripperInputStream prepare(int... buffer) throws IOException {
        return new UtfBomStripperInputStream(prepareStream(buffer));
    }

    @Test
    public void testUtf8() throws IOException {
        final UtfBomStripperInputStream is = prepare(0xEF, 0xBB, 0xBF, 0x61, 0x62, 0x63);
        assertEquals("UTF-8", is.getDetectedCharsetName());
        assertData(is);
        is.close();
    }

    @Test
    public void testNoBOM() throws IOException {
        final UtfBomStripperInputStream is = prepare(0x61, 0x62, 0x63);
        assertNull(is.getDetectedCharsetName());
        assertData(is);
        is.close();
    }

    private void assertData(InputStream inputStream) throws IOException {
        assertEquals("abc", new BufferedReader(new InputStreamReader(inputStream)).readLine());
    }


}

package liquibase.util;

import static junit.framework.Assert.assertEquals;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;

public class StreamUtilTest {

    @Test
    public void testGetStreamContents() throws IOException {
        byte[] contents = "TEST2".getBytes();
        ByteArrayInputStream stream = new ByteArrayInputStream(contents);
        String result = StreamUtil.getStreamContents(stream);
        assertEquals("TEST2", result);
    }

    @Test
    public void testGetReaderContents() throws IOException {
        String contents = "TEST";
        StringReader reader = new StringReader(contents);
        String result = StreamUtil.getReaderContents(reader);
        assertEquals(contents, result);
    }

}

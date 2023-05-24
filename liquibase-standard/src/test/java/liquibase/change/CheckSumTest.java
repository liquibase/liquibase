package liquibase.change;

import org.junit.Test;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.*;

public class CheckSumTest {

    @Test
    public void parse() {
        String checksumString = "3:asdf";
        CheckSum checkSum = CheckSum.parse(checksumString);
        assertEquals(3, checkSum.getVersion());
        assertEquals(checksumString, checkSum.toString());
    }

    @Test
    public void parse_null() {
        assertNull(CheckSum.parse(null));
    }

    @Test
    public void parse_v1() {
        String checksumString = "asdf";
        CheckSum checkSum = CheckSum.parse(checksumString);
        assertEquals(1, checkSum.getVersion());
        assertEquals("1:asdf", checkSum.toString());
    }

    @Test
    public void getCurrentVersion() {
        assertEquals(9, CheckSum.getCurrentVersion());
    }

    @Test
    public void compute_String() {
        String valueToHash = "asdf";
        CheckSum checkSum = CheckSum.compute(valueToHash, CheckSum.getCurrentVersion());
        assertEquals(CheckSum.getCurrentVersion(), checkSum.getVersion());
        assertNotEquals(checkSum.toString(), valueToHash);
    }

    @Test
    public void compute_String_shouldIgnoreUnknownUnicodeChar() {
        CheckSum checkSum1 = CheckSum.compute("asdfa", CheckSum.getCurrentVersion());
        CheckSum checkSum2 = CheckSum.compute("as\uFFFDdf\uFFFDa", CheckSum.getCurrentVersion());

        assertEquals(checkSum2, checkSum1);
    }

    @Test
    public void compute_Stream() {
        String valueToHash = "asdf";
        CheckSum checkSum = CheckSum.compute(new ByteArrayInputStream(valueToHash.getBytes()), false, CheckSum.getCurrentVersion());
        assertEquals(CheckSum.getCurrentVersion(), checkSum.getVersion());
        assertNotEquals(checkSum.toString(), valueToHash);
        assertEquals(CheckSum.compute(valueToHash, CheckSum.getCurrentVersion()).toString(), checkSum.toString());
    }

    @Test
    public void toString_test() {
        assertTrue(CheckSum.parse("9:asdf").toString().startsWith("9:"));
    }

    @Test
    public void equals() {
        assertEquals(CheckSum.parse("9:asdf"), CheckSum.parse("9:asdf"));
        assertNotEquals(CheckSum.parse("9:asdf"), CheckSum.parse("8:asdf"));
        assertNotEquals(CheckSum.parse("9:asdf"), CheckSum.parse("9:qwert"));

        assertNotEquals(12, CheckSum.parse("9:asdf"));
        assertNotEquals(null, CheckSum.parse("9:asdf"));
    }

    @Test
    public void compute_lineEndingsDontMatter() {
        String checkSum = CheckSum.compute("a string\nwith\nlines", CheckSum.getCurrentVersion()).toString();
        assertEquals(checkSum, CheckSum.compute("a string\rwith\rlines", CheckSum.getCurrentVersion()).toString());
        assertEquals(checkSum, CheckSum.compute("a string\r\nwith\r\nlines", CheckSum.getCurrentVersion()).toString());
        assertEquals(checkSum, CheckSum.compute("a string\rwith\nlines", CheckSum.getCurrentVersion()).toString());

        assertNotEquals(checkSum, CheckSum.compute("a string\n\nwith\n\nlines", CheckSum.getCurrentVersion()).toString());

        assertEquals(checkSum, CheckSum.compute(new ByteArrayInputStream("a string\nwith\nlines".getBytes()), true, CheckSum.getCurrentVersion()).toString());
        assertEquals(checkSum, CheckSum.compute(new ByteArrayInputStream("a string\rwith\rlines".getBytes()), true, CheckSum.getCurrentVersion()).toString());
        assertEquals(checkSum, CheckSum.compute(new ByteArrayInputStream("a string\r\nwith\r\nlines".getBytes()), true, CheckSum.getCurrentVersion()).toString());
        assertEquals(checkSum, CheckSum.compute(new ByteArrayInputStream("a string\rwith\r\nlines".getBytes()), true, CheckSum.getCurrentVersion()).toString());
    }

    @Test
    public void compute_lineEndingsDontMatter_multiline() {
        String checkSum = CheckSum.compute("a string\n\nwith\n\nlines", CheckSum.getCurrentVersion()).toString();
        assertEquals(checkSum, CheckSum.compute("a string\r\rwith\r\rlines", CheckSum.getCurrentVersion()).toString());
        assertEquals(checkSum, CheckSum.compute("a string\r\n\r\nwith\r\n\r\nlines", CheckSum.getCurrentVersion()).toString());

        assertEquals(checkSum, CheckSum.compute(new ByteArrayInputStream("a string\n\nwith\n\nlines".getBytes()), true, CheckSum.getCurrentVersion()).toString());
        assertEquals(checkSum, CheckSum.compute(new ByteArrayInputStream("a string\r\rwith\r\rlines".getBytes()), true, CheckSum.getCurrentVersion()).toString());
        assertEquals(checkSum, CheckSum.compute(new ByteArrayInputStream("a string\r\n\r\nwith\r\n\r\nlines".getBytes()), true, CheckSum.getCurrentVersion()).toString());
    }
}

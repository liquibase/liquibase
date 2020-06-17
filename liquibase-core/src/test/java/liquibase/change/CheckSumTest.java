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
        assertEquals(8, CheckSum.getCurrentVersion());
    }

    @Test
    public void compute_String() {
        String valueToHash = "asdf";
        CheckSum checkSum = CheckSum.compute(valueToHash);
        assertEquals(CheckSum.getCurrentVersion(), checkSum.getVersion());
        assertFalse(checkSum.toString().equals(valueToHash));
    }

    @Test
    public void compute_Stream() {
        String valueToHash = "asdf";
        CheckSum checkSum = CheckSum.compute(new ByteArrayInputStream(valueToHash.getBytes()), false);
        assertEquals(CheckSum.getCurrentVersion(), checkSum.getVersion());
        assertFalse(checkSum.toString().equals(valueToHash));
        assertEquals(CheckSum.compute(valueToHash).toString(), checkSum.toString());
    }

    @Test
    public void toString_test() {
        assertTrue(CheckSum.parse("9:asdf").toString().startsWith("9:"));
    }

    @Test
    public void hashCode_test() {
        assertNotNull(CheckSum.parse("5:asdf").hashCode());
    }

    @Test
    public void equals() {
        assertTrue(CheckSum.parse("9:asdf").equals(CheckSum.parse("9:asdf")));
        assertFalse(CheckSum.parse("9:asdf").equals(CheckSum.parse("8:asdf")));
        assertFalse(CheckSum.parse("9:asdf").equals(CheckSum.parse("9:qwert")));

        assertFalse(CheckSum.parse("9:asdf").equals(12));
        assertFalse(CheckSum.parse("9:asdf").equals(null));
    }

    @Test
    public void compute_lineEndingsDontMatter() {
        String checkSum = CheckSum.compute("a string\nwith\nlines").toString();
//        assertEquals(checkSum, CheckSum.compute("a string\rwith\rlines").toString());
        assertEquals(checkSum, CheckSum.compute("a string\r\nwith\r\nlines").toString());
        assertEquals(checkSum, CheckSum.compute("a string\rwith\nlines").toString());

        assertFalse(checkSum.equals(CheckSum.compute("a string\n\nwith\n\nlines").toString()));

        assertEquals(checkSum, CheckSum.compute(new ByteArrayInputStream("a string\nwith\nlines".getBytes()), true).toString());
//        assertEquals(checkSum, CheckSum.compute(new ByteArrayInputStream("a string\rwith\rlines".getBytes()), true).toString());
        assertEquals(checkSum, CheckSum.compute(new ByteArrayInputStream("a string\r\nwith\r\nlines".getBytes()), true).toString());
//        assertEquals(checkSum, CheckSum.compute(new ByteArrayInputStream("a string\rwith\r\nlines".getBytes()), true).toString());
    }

    @Test
    public void compute_lineEndingsDontMatter_multiline() {
        String checkSum = CheckSum.compute("a string\n\nwith\n\nlines").toString();
        assertEquals(checkSum, CheckSum.compute("a string\r\rwith\r\rlines").toString());
        assertEquals(checkSum, CheckSum.compute("a string\r\n\r\nwith\r\n\r\nlines").toString());

        assertEquals(checkSum, CheckSum.compute(new ByteArrayInputStream("a string\n\nwith\n\nlines".getBytes()), true).toString());
//        assertEquals(checkSum, CheckSum.compute(new ByteArrayInputStream("a string\r\rwith\r\rlines".getBytes()), true).toString());
        assertEquals(checkSum, CheckSum.compute(new ByteArrayInputStream("a string\r\n\r\nwith\r\n\r\nlines".getBytes()), true).toString());
    }
}

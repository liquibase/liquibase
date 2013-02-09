package liquibase.change;

import static org.junit.Assert.*;
import org.junit.Test;

import java.io.ByteArrayInputStream;

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
        assertEquals(5, CheckSum.getCurrentVersion());
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
        CheckSum checkSum = CheckSum.compute(new ByteArrayInputStream(valueToHash.getBytes()));
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
}

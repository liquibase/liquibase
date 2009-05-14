package liquibase.change;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;

public class CheckSumTest {
    @Test
    public void testCompute_String() {
        String valueToHash = "asdf";
        CheckSum checkSum = CheckSum.compute(valueToHash);
        assertEquals(2, checkSum.getVersion());
        assertFalse(checkSum.toString().equals(valueToHash));
    }

    @Test
    public void testCompute_Stream() {
        String valueToHash = "asdf";
        CheckSum checkSum = CheckSum.compute(new ByteArrayInputStream(valueToHash.getBytes()));
        assertEquals(2, checkSum.getVersion());
        assertFalse(checkSum.toString().equals(valueToHash));
        assertEquals(CheckSum.compute(valueToHash).toString(), checkSum.toString());
    }

    @Test
    public void parse_v2() {
        String checksumString = "2:asdf";
        CheckSum checkSum = CheckSum.parse(checksumString);
        assertEquals(2, checkSum.getVersion());
        assertEquals(checksumString, checksumString);
    }

    @Test
    public void parse_v1() {
        String checksumString = "asdf";
        CheckSum checkSum = CheckSum.parse(checksumString);
        assertEquals(1, checkSum.getVersion());
        assertEquals("1:asdf", checkSum.toString());
    }
}

package liquibase.change;

import liquibase.ChecksumVersions;
import liquibase.integration.commandline.LiquibaseCommandLineConfiguration;
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
        assertEquals(ChecksumVersions.V9, LiquibaseCommandLineConfiguration.CHECKSUM_VERSION.getCurrentValue());
    }

    @Test
    public void compute_String() {
        String valueToHash = "asdf";
        CheckSum checkSum = CheckSum.compute(valueToHash, LiquibaseCommandLineConfiguration.CHECKSUM_VERSION.getCurrentValue());
        assertEquals(LiquibaseCommandLineConfiguration.CHECKSUM_VERSION.getCurrentValue().getVersion(), checkSum.getVersion());
        assertNotEquals(checkSum.toString(), valueToHash);
    }

    @Test
    public void compute_String_shouldIgnoreUnknownUnicodeChar() {
        CheckSum checkSum1 = CheckSum.compute("asdfa", LiquibaseCommandLineConfiguration.CHECKSUM_VERSION.getCurrentValue());
        CheckSum checkSum2 = CheckSum.compute("as\uFFFDdf\uFFFDa", LiquibaseCommandLineConfiguration.CHECKSUM_VERSION.getCurrentValue());

        assertEquals(checkSum2, checkSum1);
    }

    @Test
    public void compute_Stream() {
        String valueToHash = "asdf";
        CheckSum checkSum = CheckSum.compute(new ByteArrayInputStream(valueToHash.getBytes()), false, LiquibaseCommandLineConfiguration.CHECKSUM_VERSION.getCurrentValue());
        assertEquals(LiquibaseCommandLineConfiguration.CHECKSUM_VERSION.getCurrentValue().getVersion(), checkSum.getVersion());
        assertNotEquals(checkSum.toString(), valueToHash);
        assertEquals(CheckSum.compute(valueToHash, LiquibaseCommandLineConfiguration.CHECKSUM_VERSION.getCurrentValue()).toString(), checkSum.toString());
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
        String checkSum = CheckSum.compute("a string\nwith\nlines", LiquibaseCommandLineConfiguration.CHECKSUM_VERSION.getCurrentValue()).toString();
        assertEquals(checkSum, CheckSum.compute("a string\rwith\rlines", LiquibaseCommandLineConfiguration.CHECKSUM_VERSION.getCurrentValue()).toString());
        assertEquals(checkSum, CheckSum.compute("a string\r\nwith\r\nlines", LiquibaseCommandLineConfiguration.CHECKSUM_VERSION.getCurrentValue()).toString());
        assertEquals(checkSum, CheckSum.compute("a string\rwith\nlines", LiquibaseCommandLineConfiguration.CHECKSUM_VERSION.getCurrentValue()).toString());

        assertNotEquals(checkSum, CheckSum.compute("a string\n\nwith\n\nlines", LiquibaseCommandLineConfiguration.CHECKSUM_VERSION.getCurrentValue()).toString());

        assertEquals(checkSum, CheckSum.compute(new ByteArrayInputStream("a string\nwith\nlines".getBytes()), true, LiquibaseCommandLineConfiguration.CHECKSUM_VERSION.getCurrentValue()).toString());
        assertEquals(checkSum, CheckSum.compute(new ByteArrayInputStream("a string\rwith\rlines".getBytes()), true, LiquibaseCommandLineConfiguration.CHECKSUM_VERSION.getCurrentValue()).toString());
        assertEquals(checkSum, CheckSum.compute(new ByteArrayInputStream("a string\r\nwith\r\nlines".getBytes()), true, LiquibaseCommandLineConfiguration.CHECKSUM_VERSION.getCurrentValue()).toString());
        assertEquals(checkSum, CheckSum.compute(new ByteArrayInputStream("a string\rwith\r\nlines".getBytes()), true, LiquibaseCommandLineConfiguration.CHECKSUM_VERSION.getCurrentValue()).toString());
    }

    @Test
    public void compute_lineEndingsDontMatter_multiline() {
        String checkSum = CheckSum.compute("a string\n\nwith\n\nlines", LiquibaseCommandLineConfiguration.CHECKSUM_VERSION.getCurrentValue()).toString();
        assertEquals(checkSum, CheckSum.compute("a string\r\rwith\r\rlines", LiquibaseCommandLineConfiguration.CHECKSUM_VERSION.getCurrentValue()).toString());
        assertEquals(checkSum, CheckSum.compute("a string\r\n\r\nwith\r\n\r\nlines", LiquibaseCommandLineConfiguration.CHECKSUM_VERSION.getCurrentValue()).toString());

        assertEquals(checkSum, CheckSum.compute(new ByteArrayInputStream("a string\n\nwith\n\nlines".getBytes()), true, LiquibaseCommandLineConfiguration.CHECKSUM_VERSION.getCurrentValue()).toString());
        assertEquals(checkSum, CheckSum.compute(new ByteArrayInputStream("a string\r\rwith\r\rlines".getBytes()), true, LiquibaseCommandLineConfiguration.CHECKSUM_VERSION.getCurrentValue()).toString());
        assertEquals(checkSum, CheckSum.compute(new ByteArrayInputStream("a string\r\n\r\nwith\r\n\r\nlines".getBytes()), true, LiquibaseCommandLineConfiguration.CHECKSUM_VERSION.getCurrentValue()).toString());
    }
}

package liquibase.change;

import liquibase.ChecksumVersion;
import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.checksums.ChecksumAlgorithm;
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
    public void getDefaultAlgorithmIsMd5() {
        assertEquals(GlobalConfiguration.CHECKSUM_ALGORITHM.getDefaultValue(), ChecksumAlgorithm.MD5);
    }

    @Test
    public void sha1Checksum() throws Exception {
        Scope.child(GlobalConfiguration.CHECKSUM_ALGORITHM.getKey(), ChecksumAlgorithm.SHA1, () -> {
            CheckSum checkSum = CheckSum.compute("asdf");
            assertEquals(ChecksumVersion.V9.getVersion(), checkSum.getVersion());
            assertEquals("3da541559918a808c2402bba5012f6c60b27661c", checkSum.getStoredCheckSum());
        });
    }

    @Test
    public void sha256Checksum() throws Exception {
        Scope.child(GlobalConfiguration.CHECKSUM_ALGORITHM.getKey(), ChecksumAlgorithm.SHA256, () -> {
            CheckSum checkSum = CheckSum.compute("asdf");
            assertEquals(ChecksumVersion.V9.getVersion(), checkSum.getVersion());
            assertEquals("f0e4c2f76c58916ec258f246851bea091d14d4247a2fc3e18694461b1816e13b", checkSum.getStoredCheckSum());
        });
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
        assertEquals(ChecksumVersion.V9, ChecksumVersion.latest());
    }

    @Test
    public void compute_String() {
        String valueToHash = "asdf";
        CheckSum checkSum = CheckSum.compute(valueToHash);
        assertEquals(ChecksumVersion.latest().getVersion(), checkSum.getVersion());
        assertNotEquals(checkSum.toString(), valueToHash);
    }

    @Test
    public void compute_String_shouldIgnoreUnknownUnicodeChar() {
        CheckSum checkSum1 = CheckSum.compute("asdfa");
        CheckSum checkSum2 = CheckSum.compute("as\uFFFDdf\uFFFDa");

        assertEquals(checkSum2, checkSum1);
    }

    @Test
    public void compute_Stream() {
        String valueToHash = "asdf";
        CheckSum checkSum = CheckSum.compute(new ByteArrayInputStream(valueToHash.getBytes()), false);
        assertEquals(ChecksumVersion.latest().getVersion(), checkSum.getVersion());
        assertNotEquals(checkSum.toString(), valueToHash);
        assertEquals(CheckSum.compute(valueToHash).toString(), checkSum.toString());
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
        String checkSum = CheckSum.compute("a string\nwith\nlines").toString();
        assertEquals(checkSum, CheckSum.compute("a string\rwith\rlines").toString());
        assertEquals(checkSum, CheckSum.compute("a string\r\nwith\r\nlines").toString());
        assertEquals(checkSum, CheckSum.compute("a string\rwith\nlines").toString());

        assertNotEquals(checkSum, CheckSum.compute("a string\n\nwith\n\nlines").toString());

        assertEquals(checkSum, CheckSum.compute(new ByteArrayInputStream("a string\nwith\nlines".getBytes()), true).toString());
        assertEquals(checkSum, CheckSum.compute(new ByteArrayInputStream("a string\rwith\rlines".getBytes()), true).toString());
        assertEquals(checkSum, CheckSum.compute(new ByteArrayInputStream("a string\r\nwith\r\nlines".getBytes()), true).toString());
        assertEquals(checkSum, CheckSum.compute(new ByteArrayInputStream("a string\rwith\r\nlines".getBytes()), true).toString());
    }

    @Test
    public void compute_lineEndingsDontMatter_multiline() {
        String checkSum = CheckSum.compute("a string\n\nwith\n\nlines").toString();
        assertEquals(checkSum, CheckSum.compute("a string\r\rwith\r\rlines").toString());
        assertEquals(checkSum, CheckSum.compute("a string\r\n\r\nwith\r\n\r\nlines").toString());

        assertEquals(checkSum, CheckSum.compute(new ByteArrayInputStream("a string\n\nwith\n\nlines".getBytes()), true).toString());
        assertEquals(checkSum, CheckSum.compute(new ByteArrayInputStream("a string\r\rwith\r\rlines".getBytes()), true).toString());
        assertEquals(checkSum, CheckSum.compute(new ByteArrayInputStream("a string\r\n\r\nwith\r\n\r\nlines".getBytes()), true).toString());
    }
}

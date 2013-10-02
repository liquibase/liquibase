package liquibase.util;

import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.logging.LogFactory;

import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;

/**
 * Generates md5-sums based on a string.
 */
public class MD5Util {

    /**
     * Used to build output as Hex
     */
    private static final char[] DIGITS_LOWER = {
        '0', '1', '2', '3', '4', '5', '6', '7',
           '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };

    public static String computeMD5(String input) {
        if (input == null) {
            return null;
        }
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
            digest.update(input.getBytes("UTF-8"));
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }
        byte[] digestBytes = digest.digest();

        String returnString = new String(encodeHex(digestBytes));
        LogFactory.getLogger().debug("Computed checksum for "+input+" as "+returnString);
        return returnString;

    }

    public static String computeMD5(InputStream stream) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");

            DigestInputStream digestStream = new DigestInputStream(stream, digest);
            while (digestStream.read() != -1) {
                ; //digest is updating
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        byte[] digestBytes = digest.digest();

        String returnString = new String(encodeHex(digestBytes));

        LogFactory.getLogger().debug("Computed checksum for "+returnString+" as "+returnString);
        return returnString;
    }

    /**
     * Converts an array of bytes into an array of characters representing the hexadecimal values of each byte in order.
     * The returned array will be double the length of the passed array, as it takes two characters to represent any
     * given byte.
     *
     * @param data
     *            a byte[] to convert to Hex characters
     * @return A char[] containing hexadecimal characters
     */
    private static char[] encodeHex(byte[] data) {

        int l = data.length;

        char[] out = new char[l << 1];

        // two characters form the hex value.
        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = DIGITS_LOWER[(0xF0 & data[i]) >>> 4];
            out[j++] = DIGITS_LOWER[0x0F & data[i]];
        }

        return out;
    }

}

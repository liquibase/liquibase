package liquibase.util;

import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;

/**
 * Generates md5-sums based on a string.
 */
public class MD5Util {
    public static String computeMD5(String input) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
            digest.update(input.getBytes("UTF-8"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        byte[] digestBytes = digest.digest();

        StringBuffer hexString = new StringBuffer();
        for (byte digestByte : digestBytes) {
            hexString.append(Integer.toHexString(0xFF & digestByte));
        }
        return hexString.toString();

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

        StringBuffer hexString = new StringBuffer();
        for (byte digestByte : digestBytes) {
            hexString.append(Integer.toHexString(0xFF & digestByte));
        }
        return hexString.toString();
    }
}

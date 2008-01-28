package liquibase.util;

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
}

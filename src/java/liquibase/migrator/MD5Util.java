package liquibase.migrator;

import java.security.MessageDigest;

public class MD5Util {
    public static String computeMD5(String input) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("MD5");
            digest.update(input.getBytes("UTF-8"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        byte[] digestBytes = digest.digest();

        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < digestBytes.length; i++) {
            hexString.append(Integer.toHexString(0xFF & digestBytes[i]));
        }
        return hexString.toString();

    }
}

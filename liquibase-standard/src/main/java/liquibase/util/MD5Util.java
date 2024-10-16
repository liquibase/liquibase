package liquibase.util;

import liquibase.checksums.ChecksumAlgorithm;
import liquibase.checksums.HashingUtil;

import java.io.InputStream;

/**
 * Generates md5-sums based on a string.
 * @deprecated Use {@link HashingUtil} instead
 */
@Deprecated
public class MD5Util {

    public static String computeMD5(String input) {
        return HashingUtil.compute(input, ChecksumAlgorithm.MD5.getAlgorithm());

    }

    public static String computeMD5(InputStream stream) {
        return HashingUtil.compute(stream, ChecksumAlgorithm.MD5.getAlgorithm());
    }

}

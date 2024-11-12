package liquibase.checksums;

import liquibase.ChecksumVersion;
import liquibase.GlobalConfiguration;
import liquibase.Scope;

import java.io.InputStream;

/**
 * Utility class for computing checksums based on Liquibase Global configuration parameter
 */
public class ComputeChecksumService {

    private ComputeChecksumService() {
        // prevent instantiation
    }

    public static String compute(String input) {
        // If the checksum version is lower or equal to V8, use MD5 algorithm as this option wasn't present back then
        if (Scope.getCurrentScope().getChecksumVersion().lowerOrEqualThan(ChecksumVersion.V8) ) {
            return HashingUtil.compute(input, ChecksumAlgorithm.MD5.getAlgorithm());
        }
        return HashingUtil.compute(input, GlobalConfiguration.CHECKSUM_ALGORITHM.getCurrentValue().getAlgorithm());
    }

    public static String compute(InputStream stream) {
        // If the checksum version is lower or equal to V8, use MD5 algorithm as this option wasn't present back then
        if (Scope.getCurrentScope().getChecksumVersion().lowerOrEqualThan(ChecksumVersion.V8)) {
            return HashingUtil.compute(stream, ChecksumAlgorithm.MD5.getAlgorithm());
        }
        return HashingUtil.compute(stream, GlobalConfiguration.CHECKSUM_ALGORITHM.getCurrentValue().getAlgorithm());
    }
}

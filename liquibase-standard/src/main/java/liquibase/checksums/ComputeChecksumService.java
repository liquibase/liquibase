package liquibase.checksums;

import liquibase.ChecksumVersion;
import liquibase.GlobalConfiguration;
import liquibase.Scope;
import lombok.Getter;

import java.io.InputStream;

public class ComputeChecksumService {

    @Getter
    private static final ComputeChecksumService instance = new ComputeChecksumService();

    public String compute(String input) {
        if (Scope.getCurrentScope().getChecksumVersion().lowerOrEqualThan(ChecksumVersion.V9)) {
            return HashingUtil.compute(input, ChecksumAlgorithm.MD5.getAlgorithm());
        }
        return HashingUtil.compute(input, GlobalConfiguration.CHECKSUM_ALGORITHM.getCurrentValue().getAlgorithm());
    }

    public String compute(InputStream stream) {
        if (Scope.getCurrentScope().getChecksumVersion().lowerOrEqualThan(ChecksumVersion.V9)) {
            return HashingUtil.compute(stream, ChecksumAlgorithm.MD5.getAlgorithm());
        }
        return HashingUtil.compute(stream, GlobalConfiguration.CHECKSUM_ALGORITHM.getCurrentValue().getAlgorithm());
    }
}

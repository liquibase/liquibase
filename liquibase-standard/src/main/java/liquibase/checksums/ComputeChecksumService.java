package liquibase.checksums;

import liquibase.GlobalConfiguration;
import lombok.Getter;

import java.io.InputStream;

public class ComputeChecksumService {

    @Getter
    private static final ComputeChecksumService instance = new ComputeChecksumService();

    public String compute(String input) {
        return HashingUtil.compute(input, GlobalConfiguration.CHECKSUM_ALGORITHM.getCurrentValue().getAlgorithm());
    }

    public String compute(InputStream stream) {
        return HashingUtil.compute(stream, GlobalConfiguration.CHECKSUM_ALGORITHM.getCurrentValue().getAlgorithm());
    }
}

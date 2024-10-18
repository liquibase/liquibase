package liquibase.checksums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ChecksumAlgorithm {
    MD5("MD5", 32),
    SHA1("SHA-1", 40),
    SHA256("SHA-256", 64);

    private final String algorithm;

    private final int size;

}

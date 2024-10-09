package liquibase.checksums;

import lombok.Getter;

@Getter
public enum ChecksumAlgorithm {
    MD5("MD5"),
    SHA1("SHA-1"),
    SHA256("SHA-256");

    private final String algorithm;

    ChecksumAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

}

package liquibase;

import liquibase.exception.UnsupportedChecksumVersionException;

import java.util.Arrays;

/**
 * Enum used to keep track of Liquibase Checksum versions introduced to enable the support of multiple versions at the same time.
 */
public enum ChecksumVersion {

    V1(1, "Pass through version for testing purpose", "0"),
    V8(8, "Version used from Liquibase 3.5.0 until 4.21.1", "3.5.0"),
    V9(9, "Version used from Liquibase 4.22.0 till now", "4.22.0");

    private final int version;

    private final String since;
    private final String description;

    ChecksumVersion(int version, String description, String since) {
        this.version = version;
        this.description = description;
        this.since = since;
    }

    public int getVersion() {
        return version;
    }

    public String getDescription() {
        return description;
    }

    public String getSince() {
        return since;
    }

    public static ChecksumVersion enumFromChecksumVersion(int i) {
        if (i != 1 && i < 8 || i > 9) {
            throw new UnsupportedChecksumVersionException(i);
        }
        return Arrays.stream(ChecksumVersion.values()).filter(cv -> cv.getVersion() == i)
                .findFirst().orElse(null);
    }
}

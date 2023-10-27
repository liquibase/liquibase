package liquibase;

import liquibase.exception.UnsupportedChecksumVersionException;
import lombok.Getter;

import java.util.Arrays;

/**
 * Enum used to keep track of Liquibase Checksum versions introduced to enable the support of multiple versions at the same time.
 */
@Getter
public enum ChecksumVersion {

    V9(9, "Version used from Liquibase 4.22.0 till now", "4.22.0"),
    V8(8, "Version used from Liquibase 3.5.0 until 4.21.1", "3.5.0"),
    V7(7, "Old version", "?"),
    V6(6, "Old version", "?"),
    V5(5, "Old version", "?"),
    V4(4, "Old version", "?"),
    V3(3, "Old version", "?"),
    V2(2, "Old version", "?"),
    V1(1, "Pass through version for testing purpose", "0");


    private final int version;

    private final String since;
    private final String description;

    ChecksumVersion(int version, String description, String since) {
        this.version = version;
        this.description = description;
        this.since = since;
    }

    public static ChecksumVersion latest() {
        return Scope.getCurrentScope().get(Scope.Attr.latestChecksumVersion, ChecksumVersion.class);
    }

    public static ChecksumVersion enumFromChecksumVersion(int i) {
        return Arrays.stream(ChecksumVersion.values()).filter(cv -> cv.getVersion() == i).findFirst()
                .orElseThrow(() -> new UnsupportedChecksumVersionException(i));
    }

    public boolean lowerOrEqualThan(ChecksumVersion compareTo) {
        return compareTo != null && this.version <= compareTo.getVersion();
    }
}

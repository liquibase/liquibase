package liquibase;

import liquibase.exception.UnsupportedChecksumVersionException;

import java.util.Arrays;

public enum ChecksumVersions {

    V8(8, "Version used from Liquibase 3.5.0 until 4.21.1"),
    V9(9, "Version used from Liquibase 4.22.0 till now");

    private final int version;
    private final String description;

    ChecksumVersions(int version, String description) {
        this.version = version;
        this.description = description;
    }

    public int getVersion() {
        return version;
    }

    public String getDescription() {
        return description;
    }

    public static ChecksumVersions latest() {
        return V9;
    }

    public static ChecksumVersions enumFromChecksumVersion(int i) {
        if (i < 8 || i > 9) {
            throw new UnsupportedChecksumVersionException(i);
        }
        return Arrays.stream(ChecksumVersions.values()).filter(cv -> cv.getVersion() == i)
                .findFirst().orElse(null);
    }
}

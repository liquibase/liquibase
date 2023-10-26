package liquibase.checksum;

import liquibase.ChecksumVersion;

public class DefaultLatestChecksumVersionPlugin implements LatestChecksumVersionPlugin {

    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    @Override
    public ChecksumVersion getChecksumVersion() {
        return ChecksumVersion.V9;
    }
}

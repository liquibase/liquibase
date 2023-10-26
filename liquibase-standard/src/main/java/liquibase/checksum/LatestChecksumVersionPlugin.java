package liquibase.checksum;

import liquibase.ChecksumVersion;
import liquibase.plugin.Plugin;

/**
 * This service is used to obtain the {@link ChecksumVersion} enum through a {@link LatestChecksumVersionPlugin}
 * that represents the latest version that Liquibase should use.
 */
public interface LatestChecksumVersionPlugin extends Plugin {

    int getPriority();

    ChecksumVersion getChecksumVersion();


}

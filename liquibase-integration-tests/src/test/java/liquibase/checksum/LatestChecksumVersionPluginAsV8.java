package liquibase.checksum;

import liquibase.ChecksumVersion;

public class LatestChecksumVersionPluginAsV8 implements LatestChecksumVersionPlugin {

        @Override
        public int getPriority() {
            return PRIORITY_DEFAULT - 10;
        }

        @Override
        public ChecksumVersion getChecksumVersion() {
            return ChecksumVersion.V8;
        }
    }

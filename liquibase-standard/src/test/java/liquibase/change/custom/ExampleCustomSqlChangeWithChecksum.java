package liquibase.change.custom;

import liquibase.change.CheckSum;

public class ExampleCustomSqlChangeWithChecksum extends ExampleCustomSqlChange implements CustomChangeChecksum {

    // Some synthetic field to be used in our checksum calculation
    private final Integer version = 5;

    /**
     * Generate a checksum based on the classname and the version number within.
     * Does not care about any parameters set
     * @return the calculated checksum
     */
    @Override
    public CheckSum generateChecksum() {
        return CheckSum.compute(getClass().getName() + ":" + version);
    }
}

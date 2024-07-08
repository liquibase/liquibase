package liquibase.change.custom;

import liquibase.change.AbstractChange;
import liquibase.change.CheckSum;

/**
 * Interface to implement that allows a custom change to generate its own checksum.
 *
 * @see liquibase.change.custom.CustomChange
 * @see AbstractChange#generateCheckSum()
 */
public interface CustomChangeChecksum {

    /**
     * Generates a checksum for the current state of the change.
     *
     * @return the generated checksum
     */
    CheckSum generateChecksum();

}

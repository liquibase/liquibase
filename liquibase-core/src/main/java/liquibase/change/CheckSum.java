package liquibase.change;

import liquibase.util.MD5Util;

import java.io.InputStream;

/**
 * CheckSums are used by liquibase to determine if a Change has been modified since it was originally ran.
 * CheckSums can be computed on either a String or an {@link InputStream}.
 * The CheckSum contains a version number which can be used to determine whether the algorithm for computing a checksum has changed
 * since the last time it was computed. If the algorithm changes, we cannot rely on the checksum value.
 * <p></p>
 * It is not up to this class to determine what should be checksum-ed, it simply hashes what is passed to it.
 */
public class CheckSum {
    private int version;
    private String checksum;

    private CheckSum(String checksum, int version) {
        this.checksum = checksum;
        this.version = version;
    }

    /**
     * Parse the given checksum string value into a CheckSum object.
     */
    public static CheckSum parse(String checksumValue) {
        if (checksumValue == null) {
            return null;
        }
        if (checksumValue.matches("^\\d:.*")) {
            return new CheckSum(checksumValue.substring(2), Integer.valueOf(checksumValue.substring(0,1)));
        } else {
            return new CheckSum(checksumValue, 1);
        }
    }

    /**
     * Return the current CheckSum algorithm version.
     */
    public static int getCurrentVersion() {
        return 5;
    }

    /**
     * Compute a checksum of the given string.
     */
    public static CheckSum compute(String valueToChecksum) {
        return new CheckSum(MD5Util.computeMD5(valueToChecksum), getCurrentVersion());
    }

    /**
     * Compute a checksum of the given data stream.
     */
    public static CheckSum compute(InputStream stream) {
        return new CheckSum(MD5Util.computeMD5(stream), getCurrentVersion());
    }

    @Override
    public String toString() {
        return version+":"+this.checksum;
    }

    /**
     * Return the Checksum Algorithm version for this CheckSum
     */
    public int getVersion() {
        return version;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof CheckSum && this.toString().equals(obj.toString());
    }
}

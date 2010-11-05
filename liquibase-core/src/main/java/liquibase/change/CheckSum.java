package liquibase.change;

import liquibase.util.MD5Util;

import java.io.InputStream;

public class CheckSum {
    private int version;
    private String checksum;

    private CheckSum(String checksum, int version) {
        this.checksum = checksum;
        this.version = version;
    }

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

    public static int getCurrentVersion() {
        return 3;
    }

    public static CheckSum compute(String valueToChecksum) {
        return new CheckSum(MD5Util.computeMD5(valueToChecksum), getCurrentVersion());
    }

    public static CheckSum compute(InputStream stream) {
        return new CheckSum(MD5Util.computeMD5(stream), getCurrentVersion());
    }

    @Override
    public String toString() {
        return version+":"+this.checksum;
    }

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

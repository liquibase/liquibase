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
        if (checksumValue.startsWith("2:")) {
            return new CheckSum(checksumValue.substring(2), 2);
        } else {
            return new CheckSum(checksumValue, 1);
        }
    }

    public static CheckSum compute(String valueToChecksum) {
        return new CheckSum(MD5Util.computeMD5(valueToChecksum), 2);
    }

    public static CheckSum compute(InputStream stream) {
        return new CheckSum(MD5Util.computeMD5(stream), 2);
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

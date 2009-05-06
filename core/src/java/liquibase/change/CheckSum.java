package liquibase.change;

import liquibase.util.MD5Util;

import java.io.InputStream;

public class CheckSum {
    private int version = 2;
    private String checksum;

    public CheckSum(String valueToChecksum) {
        this.checksum = MD5Util.computeMD5(valueToChecksum);
    }

    public CheckSum(InputStream stream) {
        this.checksum = MD5Util.computeMD5(stream);
    }

    @Override
    public String toString() {
        return version+":"+this.checksum;
    }

    public int getVersion() {
        return version;
    }
}

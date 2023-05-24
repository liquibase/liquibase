package liquibase.exception;

public class UnsupportedChecksumVersionException extends RuntimeException {

    private static final long serialVersionUID = -229400973681987065L;

    public UnsupportedChecksumVersionException(int i) {
        super("Unsupported Checksum version: " + i);
    }
}

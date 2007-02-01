package liquibase.migrator;

public class MigrationFailedException extends Exception {
    public MigrationFailedException() {
    }

    public MigrationFailedException(String message) {
        super(message);
    }

    public MigrationFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public MigrationFailedException(Throwable cause) {
        super(cause);
    }
}

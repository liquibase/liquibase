package liquibase.migrator.exception;

public class MigrationFailedException extends LiquibaseException {

    private static final long serialVersionUID = 1L;
    
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

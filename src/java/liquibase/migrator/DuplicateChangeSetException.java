package liquibase.migrator;

public class DuplicateChangeSetException extends Exception{
    public DuplicateChangeSetException(String message) {
        super(message);
    }
}

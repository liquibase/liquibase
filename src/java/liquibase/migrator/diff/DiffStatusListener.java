package liquibase.migrator.diff;

public interface DiffStatusListener {
    void statusUpdate(String message);
}

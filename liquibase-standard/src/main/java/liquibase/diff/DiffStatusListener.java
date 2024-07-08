package liquibase.diff;

public interface DiffStatusListener {
    void statusUpdate(String message);
}

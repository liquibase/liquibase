package liquibase.logging;

public interface LoggerContext extends AutoCloseable {

    @Override
    void close();

    void showProgress();

    void showProgress(int percentComplete);
}

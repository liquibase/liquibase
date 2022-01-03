package liquibase.extension.testing.testsystem.wrapper;

public abstract class DatabaseWrapper {

    public abstract void start(boolean keepRunning) throws Exception;

    public abstract String getUsername();

    public abstract String getUrl();

}

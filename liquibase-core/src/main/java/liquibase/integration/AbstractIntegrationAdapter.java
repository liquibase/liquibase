package liquibase.integration;

public abstract class AbstractIntegrationAdapter {

    public abstract String[] getCommand();

    public abstract String getUserErrorMessage(String message, Throwable exception);
}

package liquibase.logging;

public enum LogFormat {
    TEXT(false),
    JSON(true),
    JSON_PRETTY(true);

    private final boolean useScopeLoggerInMaven;

    LogFormat(boolean useScopeLoggerInMaven) {
        this.useScopeLoggerInMaven = useScopeLoggerInMaven;
    }

    public boolean isUseScopeLoggerInMaven() {
        return useScopeLoggerInMaven;
    }
}

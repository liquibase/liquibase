package liquibase.logging;

import lombok.Getter;

@Getter
public enum LogFormat {
    TEXT(false),
    JSON(true),
    JSON_PRETTY(true);

    private final boolean useScopeLoggerInMaven;

    LogFormat(boolean useScopeLoggerInMaven) {
        this.useScopeLoggerInMaven = useScopeLoggerInMaven;
    }

}

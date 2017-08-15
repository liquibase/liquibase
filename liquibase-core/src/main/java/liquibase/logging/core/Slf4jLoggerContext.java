package liquibase.logging.core;

import liquibase.logging.LoggerContext;
import org.slf4j.MDC;

public class Slf4jLoggerContext implements LoggerContext {

    private final String key;

    public Slf4jLoggerContext(String key, Object value) {
        MDC.put(key, String.valueOf(value));
        this.key = key;
    }

    @Override
    public void showMoreProgress() {

    }

    @Override
    public void showMoreProgress(int percentComplete) {

    }

    @Override
    public void close() {
        MDC.remove(key);
    }
}

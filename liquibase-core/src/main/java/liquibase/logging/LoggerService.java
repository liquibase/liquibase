package liquibase.logging;

public interface LoggerService {
    Logger getLog(String name);

    Logger getLog(Class clazz);

    LoggerContext pushContext(Object object);
}

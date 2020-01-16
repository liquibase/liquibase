package liquibase.command;

import liquibase.exception.LiquibaseException;

import java.util.Map;

public abstract class AbstractSelfConfiguratingCommand<T extends CommandResult> extends AbstractCommand<T> {
    public static final String CHANGE_SET_ID = "changeSetId";
    public static final String CHANGE_SET_AUTHOR = "changeSetAuthor";
    public static final String CHANGE_SET_PATH = "changeSetPath";
    public static final String ROLLBACK_SCRIPT = "rollbackScript";
    public static final String DATABASE = "database";
    public static final String CHANGELOG = "changeLog";
    public static final String RESOURCE_ACCESSOR = "resourceAccessor";
    public static final String CHANGE_LOG_PARAMETERS = "changeLogParameters";
    public static final String CHANGE_LOG_FILE = "changeLogFile";
    public static final String FORCE = "force";

    public void configure(Map<String, Object> argsMap) throws LiquibaseException {

    }
}
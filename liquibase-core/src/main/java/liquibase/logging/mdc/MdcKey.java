package liquibase.logging.mdc;

public class MdcKey {
    public static final String DEPLOYMENT_ID = "deploymentId";
    public static final String CHANGESET_ID = "changesetId";
    public static final String CHANGESET_AUTHOR = "changesetAuthor";
    public static final String  CHANGESET_FILEPATH = "changesetFilepath";
    public static final String CHANGESET_OUTCOME = "changesetOutcome";
    public static final String OPERATION_TYPE = "liquibaseOperation";
    public static final String OPERATION_TARGET_TYPE = "liquibaseTargetType";
    public static final String OPERATION_TARGET_VALUE = "liquibaseTarget";
    public static final String CHANGESET_OPERATION_START_TIME = "changesetOperationStart";
    public static final String CHANGESET_OPERATION_STOP_TIME = "changesetOperationStop";
    public static final String CHANGESET_SQL = "changesetSql";
    public static final String DEPLOYMENT_OUTCOME = "deploymentOutcome";
    public static final String LIQUIBASE_COMMAND_NAME = "liquibaseCommandName";
    public static final String LIQUIBASE_VERSION = "liquibaseVersion";
    public static final String LIQUIBASE_SYSTEM_NAME = "liquibaseSystemName";
    public static final String LIQUIBASE_SYSTEM_USER = "liquibaseSystemUser";
}

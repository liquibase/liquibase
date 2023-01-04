package liquibase.logging.mdc;

public enum MdcKey {
    DEPLOYMENT_ID("deploymentId"),
    CHANGESET_ID("changesetId"),
    CHANGESET_AUTHOR("changesetAuthor"),
    CHANGESET_FILEPATH("changesetFilepath"),
    CHANGESET_OUTCOME("changesetOutcome"),
    OPERATION_TYPE("liquibaseOperation"),
    OPERATION_TARGET_TYPE("liquibaseTargetType"),
    OPERATION_TARGET_VALUE("liquibaseTarget"),
    CHANGESET_OPERATION_START_TIME("changesetOperationStart"),
    CHANGESET_OPERATION_STOP_TIME("changesetOperationStop"),
    CHANGESET_SQL("changesetSql"),
    CHANGESET_OPERATION_MESSAGE("changesetOperationMessage"),
    CHANGESET_DATABASE_OUTPUT("changesetDatabaseOutput"),
    DEPLOYMENT_OUTCOME("deploymentOutcome"),
    LIQUIBASE_COMMAND_NAME("liquibaseCommandName");

    private final String key;

    MdcKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return this.key;
    }
}

package liquibase.logging.mdc;

public class MdcKey {
    public static final String DEPLOYMENT_ID = "deploymentId";
    public static final String CHANGESET_ID = "changesetId";
    public static final String CHANGESET_AUTHOR = "changesetAuthor";
    public static final String CHANGESET_FILEPATH = "changesetFilepath";
    public static final String CHANGESET_OUTCOME = "changesetOutcome";
    public static final String OPERATION_TYPE = "liquibaseOperation";
    public static final String LIQUIBASE_TARGET_URL = "liquibaseTargetUrl";
    public static final String LIQUIBASE_REF_URL = "liquibaseRefUrl";
    public static final String CHANGESET_OPERATION_START_TIME = "changesetOperationStart";
    public static final String CHANGESET_OPERATION_STOP_TIME = "changesetOperationStop";
    public static final String CHANGESET_SQL = "changesetSql";
    public static final String DEPLOYMENT_OUTCOME = "deploymentOutcome";
    public static final String LIQUIBASE_COMMAND_NAME = "liquibaseCommandName";
    public static final String LIQUIBASE_VERSION = "liquibaseVersion";
    public static final String LIQUIBASE_SYSTEM_NAME = "liquibaseSystemName";
    public static final String LIQUIBASE_SYSTEM_USER = "liquibaseSystemUser";
    public static final String ROLLBACK_TO_TAG = "rollbackToTag";
    public static final String CHANGELOG_FILE = "changelogFile";
    public static final String ROLLBACK_SCRIPT = "rollbackScript";
    public static final String CHANGESET_COMMENT = "changesetComment";
    public static final String CHANGESET_LABEL = "changesetLabel";
    public static final String CHANGESET_LABEL_FILTER = "changesetLabelFilter";
    public static final String CHANGESET_CONTEXT = "changesetContext";
    public static final String CHANGESET_CONTEXT_FILTER = "changesetContextFilter";
    public static final String DEPLOYMENT_CHANGESET_COUNT = "deploymentOutcomeCount";
    public static final String CHANGELOG_PROPERTIES = "changelogProperties";
    public static final String ROLLBACK_COUNT = "rollbackCount";
    public static final String CHANGESETS_ROLLED_BACK = "changesetsRolledback";
    public static final String ROLLBACK_ONE_CHANGESET_FORCE = "rollbackOneChangesetForce";
}

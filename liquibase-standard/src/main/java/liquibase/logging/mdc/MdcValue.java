package liquibase.logging.mdc;

public class MdcValue {
    public static final String COMMAND_SUCCESSFUL = "success";
    public static final String COMMAND_FAILED = "fail";
    public static final String URL_DATABASE_TARGET = "url";
    public static final String DATABASE_CHANGELOG_OUTCOME_SUCCESS = "executed";
    public static final String DATABASE_CHANGELOG_OUTCOME_FAILED = "failed";
}

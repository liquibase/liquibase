package liquibase.report;

import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.logging.mdc.customobjects.ExceptionDetails;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.util.Date;

@Data
public class RollbackReportParameters implements UpdateRollbackReportParameters {
    private String changelogArgValue;
    private String jdbcUrl;
    private Integer rollbackCount;
    private String rollbackTag;
    private String rollbackDate;
    private String failedChangeset;
    private String deploymentId;
    private ChangesetDetails changesetDetails;
    private Boolean success = Boolean.TRUE; // assume success until we know we failed
    private String commandTitle = "Rollback";
    private final DatabaseInfo databaseInfo = new DatabaseInfo();
    private final RuntimeInfo runtimeInfo = new RuntimeInfo();
    private final OperationInfo operationInfo = new OperationInfo();
    private final CustomData customData = new CustomData();
    private final ChangesetInfo changesetInfo = new ChangesetInfo();
    private final Date date = new Date();

    /**
     * Setup database related info used in rollback report.
     *
     * @param database the database the report is run against
     * @throws DatabaseException if unable to determine the database product version
     */
    public void setupDatabaseInfo(Database database) throws DatabaseException {
        this.getDatabaseInfo().setDatabaseType(database.getDatabaseProductName());
        this.getDatabaseInfo().setVersion(database.getDatabaseProductVersion());
        this.getDatabaseInfo().setDatabaseUrl(database.getConnection().getURL());
        this.setJdbcUrl(database.getConnection().getURL());
    }

    @Getter
    @AllArgsConstructor
    public static class ChangesetDetails {
        private String id;
        private String author;
        private String path;
    }

    public void setRollbackException(ExceptionDetails exceptionDetails) {
        // Set the exception similar to how it is formatted in the console.
        // The double newline is intentional.
        this.getOperationInfo().setException(String.format("%s\n%s\n%s\n\n%s",
                exceptionDetails.getFormattedPrimaryException(),
                exceptionDetails.getFormattedPrimaryExceptionReason(),
                exceptionDetails.getFormattedPrimaryExceptionSource(),
                // Intentionally not using the formatted version for this last item.
                exceptionDetails.getPrimaryExceptionReason()));
    }
}

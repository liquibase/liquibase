package liquibase.report;

import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import lombok.Data;

import java.util.Date;

@Data
public class RollbackReportParameters implements UpdateRollbackReportParameters {
    private String changelogArgValue;
    private String jdbcUrl;
    private Integer rollbackCount;
    private String rollbackTag;
    private String rollbackDate;
    private String failedChangeset;
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
}

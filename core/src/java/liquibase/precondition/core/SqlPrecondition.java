package liquibase.precondition.core;

import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.PreconditionErrorException;
import liquibase.exception.PreconditionFailedException;
import liquibase.executor.ExecutorService;
import liquibase.precondition.Precondition;
import liquibase.statement.core.RawSqlStatement;

public class SqlPrecondition implements Precondition {

    private String expectedResult;
    private String sql;


    public String getExpectedResult() {
        return expectedResult;
    }

    public void setExpectedResult(String expectedResult) {
        this.expectedResult = expectedResult;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public void check(Database database, DatabaseChangeLog changeLog) throws PreconditionFailedException, PreconditionErrorException {
        DatabaseConnection connection = database.getConnection();
        try {
            String result = (String) ExecutorService.getInstance().getExecutor(database).queryForObject(new RawSqlStatement(getSql()), String.class);
            if (result == null) {
                throw new PreconditionFailedException("No rows returned from SQL Precondition", changeLog, this);
            }

            if (!expectedResult.equals(result)) {
                throw new PreconditionFailedException("SQL Precondition failed.  Expected '"+expectedResult+"' got '"+result+"'", changeLog, this);
            }

        } catch (DatabaseException e) {
            throw new PreconditionErrorException(e, changeLog, this);
        }
    }

    public String getName() {
        return "sqlCheck";
    }
}

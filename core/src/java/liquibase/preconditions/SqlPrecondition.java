package liquibase.preconditions;

import liquibase.migrator.Migrator;
import liquibase.DatabaseChangeLog;
import liquibase.exception.PreconditionFailedException;

import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

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

    public void check(Migrator migrator, DatabaseChangeLog changeLog) throws PreconditionFailedException {
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            statement = migrator.getDatabase().getConnection().createStatement();
            resultSet = statement.executeQuery(getSql());
            if (!resultSet.next()) {
                throw new PreconditionFailedException("No rows returned from SQL Precondition", changeLog, this);
            }
            String returnString = resultSet.getString(1);
            if (resultSet.next()) {
                throw new PreconditionFailedException("Too Many rows returned from SQL Precondition", changeLog, this);
            }

            if (!expectedResult.equals(returnString)) {
                throw new PreconditionFailedException("SQL Precondition failed.  Expected '"+expectedResult+"' got '"+returnString+"'", changeLog, this);
            }

        } catch (SQLException e) {
            throw new PreconditionFailedException("Error executing SQL precondition: "+e.getMessage(), changeLog, this);
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
            } catch (SQLException e) {
                ;
            }
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException e) {
                ;
            }
        }
    }

    public String getTagName() {
        return "sqlCheck";
    }
}

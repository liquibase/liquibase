package liquibase.preconditions;

import liquibase.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.exception.PreconditionFailedException;
import liquibase.exception.PreconditionErrorException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            statement = database.getConnection().createStatement();
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
            throw new PreconditionErrorException(e, changeLog, this);
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

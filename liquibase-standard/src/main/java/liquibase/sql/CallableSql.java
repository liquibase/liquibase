package liquibase.sql;

import liquibase.structure.DatabaseObject;

import java.util.Collection;
import java.util.Collections;

public class CallableSql implements Sql {

    private String sql;
    private String endDelimiter;
    private String expectedStatus;

    public CallableSql(String sql, String expectedStatus) {
        this(sql, ";", expectedStatus);
    }

    public CallableSql(String sql, String endDelimiter, String expectedStatus) {
        this.sql = sql;
        this.endDelimiter = endDelimiter;
        this.expectedStatus = expectedStatus;
    }

    @Override
    public String toSql() {
        return sql;
    }

    @Override
    public String getEndDelimiter() {
        return endDelimiter;
    }

    public String getExpectedStatus() {
        return expectedStatus;
    }

    @Override
    public Collection<? extends DatabaseObject> getAffectedDatabaseObjects() {
        return Collections.emptyList();
    }
}

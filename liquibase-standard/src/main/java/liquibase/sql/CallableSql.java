package liquibase.sql;

import liquibase.structure.DatabaseObject;
import lombok.Getter;

import java.util.Collection;
import java.util.Collections;

public class CallableSql implements Sql {

    private final String sql;
    @Getter
    private final String endDelimiter;
    @Getter
    private final String expectedStatus;

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
    public Collection<? extends DatabaseObject> getAffectedDatabaseObjects() {
        return Collections.emptyList();
    }
}

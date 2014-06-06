package liquibase.executor;

import liquibase.sql.visitor.SqlVisitor;

import java.util.List;

/**
 * Options that can affect SQL ran against a database.
 */
public class ExecutionOptions {
    private List<SqlVisitor> sqlVisitors;

    public ExecutionOptions() {
    }

    public ExecutionOptions(List<SqlVisitor> sqlVisitors) {
        this.sqlVisitors = sqlVisitors;
    }

    public List<SqlVisitor> getSqlVisitors() {
        return sqlVisitors;
    }
}

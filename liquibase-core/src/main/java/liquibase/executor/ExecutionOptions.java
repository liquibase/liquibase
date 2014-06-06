package liquibase.executor;

import liquibase.RuntimeEnvironment;
import liquibase.sql.visitor.SqlVisitor;

import java.util.List;

/**
 * Options that can affect SQL ran against a database.
 */
public class ExecutionOptions {
    private RuntimeEnvironment runtimeEnvironment;
    private List<SqlVisitor> sqlVisitors;

    public ExecutionOptions(RuntimeEnvironment runtimeEnvironment) {
        this.runtimeEnvironment = runtimeEnvironment;
    }

    public ExecutionOptions(List<SqlVisitor> sqlVisitors, RuntimeEnvironment runtimeEnvironment) {
        this.sqlVisitors = sqlVisitors;
        this.runtimeEnvironment = runtimeEnvironment;
    }

    public List<SqlVisitor> getSqlVisitors() {
        return sqlVisitors;
    }

    public RuntimeEnvironment getRuntimeEnvironment() {
        return runtimeEnvironment;
    }
}

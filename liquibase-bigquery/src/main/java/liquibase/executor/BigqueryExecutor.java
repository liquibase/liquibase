package liquibase.executor;

import liquibase.database.BigqueryDatabase;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.executor.jvm.JdbcExecutor;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.statement.SqlStatement;

import java.util.List;

public class BigqueryExecutor extends JdbcExecutor {

    @Override
    public int getPriority() {
        return BigqueryDatabase.BIGQUERY_PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(Database database) {
        return database instanceof BigqueryDatabase;
    }

    @Override
    public void execute(SqlStatement sql) throws DatabaseException {
       super.execute(sql);
    }

    @Override
    public void execute(SqlStatement sql, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        super.execute(sql, sqlVisitors);
    }
}

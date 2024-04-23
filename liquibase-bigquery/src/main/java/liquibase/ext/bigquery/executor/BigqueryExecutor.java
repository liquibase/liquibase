package liquibase.ext.bigquery.executor;

import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.executor.jvm.JdbcExecutor;
import liquibase.ext.bigquery.database.BigqueryDatabase;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.statement.SqlStatement;

import java.util.List;

import static liquibase.ext.bigquery.database.BigqueryDatabase.BIGQUERY_PRIORITY_DATABASE;

public class BigqueryExecutor extends JdbcExecutor {

    @Override
    public int getPriority() {
        return BIGQUERY_PRIORITY_DATABASE;
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

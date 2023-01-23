package liquibase.executor.jvm;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.DatabaseException;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.SetColumnRemarksStatement;

import java.util.List;

public class SnowflakeJdbcExecutor extends JdbcExecutor {

    @Override
    public boolean supports(Database database) {
        return database instanceof SnowflakeDatabase;
    }

    @Override
    public int getPriority() {
        return PRIORITY_SPECIALIZED;
    }

    @Override
    public void execute(SqlStatement sql, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        try {
            super.execute(sql, sqlVisitors);
        } catch (DatabaseException e) {
            if (sql instanceof SetColumnRemarksStatement) {
                if (e.getMessage().contains("Object found is of type 'VIEW', not specified type 'TABLE'")) {
                    throw new DatabaseException("Snowflake does not support setting column comments on views, only tables.", e);
                }
            }
        }
    }
}

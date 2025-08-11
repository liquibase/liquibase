package liquibase.executor.jvm;

import liquibase.database.Database;
import liquibase.database.OfflineConnection;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.DatabaseException;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.SetColumnRemarksStatement;

import java.util.List;

import static liquibase.sqlgenerator.core.SetColumnRemarksGeneratorSnowflake.SET_COLUMN_REMARKS_NOT_SUPPORTED_ON_VIEW_MSG;

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
        if (database.getConnection() instanceof OfflineConnection) {
            return;
        }
        try {
            super.execute(sql, sqlVisitors);
        } catch (DatabaseException e) {
            if (sql instanceof SetColumnRemarksStatement &&
                        e.getMessage().contains("Object found is of type 'VIEW', not specified type 'TABLE'")) {
                throw new DatabaseException(SET_COLUMN_REMARKS_NOT_SUPPORTED_ON_VIEW_MSG, e);
            }
            throw e;
        }
    }
}

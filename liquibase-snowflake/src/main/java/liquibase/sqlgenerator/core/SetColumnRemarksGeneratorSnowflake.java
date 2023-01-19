package liquibase.sqlgenerator.core;

import liquibase.Scope;
import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.DatabaseException;
import liquibase.executor.ExecutorService;
import liquibase.statement.core.RawSqlStatement;
import liquibase.statement.core.SetColumnRemarksStatement;
import liquibase.util.ColumnParentType;

import java.util.List;
import java.util.Map;

public class SetColumnRemarksGeneratorSnowflake extends SetColumnRemarksGenerator {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(SetColumnRemarksStatement statement, Database database) {
        if (database instanceof SnowflakeDatabase) {
            if (statement.getColumnParentType() != null) {
                // Snowflake doesn't support setting the column remarks on a view.
                return statement.getColumnParentType() != ColumnParentType.VIEW;
            } else {
                // Check if we're trying to set the column remarks on a view, and if so, note that this is not supported.
                try {
                    List<Map<String, ?>> viewList = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database).queryForList(
                            new RawSqlStatement("SHOW VIEWS LIKE '" + statement.getTableName() + "'"));
                    return viewList.isEmpty();
                } catch (DatabaseException e) {
                    Scope.getCurrentScope().getLog(getClass()).severe("Failed to query Snowflake to determine if object is a table or view.", e);
                }
            }
        }
        return false;
    }
}

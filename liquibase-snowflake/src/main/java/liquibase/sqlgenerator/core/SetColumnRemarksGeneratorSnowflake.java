package liquibase.sqlgenerator.core;

import liquibase.Scope;
import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.DatabaseException;
import liquibase.exception.ValidationErrors;
import liquibase.executor.ExecutorService;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.RawParameterizedSqlStatement;
import liquibase.statement.core.SetColumnRemarksStatement;
import liquibase.util.ColumnParentType;

import java.util.List;
import java.util.Map;

public class SetColumnRemarksGeneratorSnowflake extends SetColumnRemarksGenerator {

    public static final String SET_COLUMN_REMARKS_NOT_SUPPORTED_ON_VIEW_MSG = "setColumnRemarks change type isn't supported on Snowflake for a 'view'";

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(SetColumnRemarksStatement statement, Database database) {
        return database instanceof SnowflakeDatabase;
    }

    @Override
    public ValidationErrors validate(SetColumnRemarksStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = super.validate(statement, database, sqlGeneratorChain);
        if (database instanceof SnowflakeDatabase) {
            if (statement.getColumnParentType() != null) {
                // Snowflake doesn't support setting the column remarks on a view.
                if (statement.getColumnParentType() == ColumnParentType.VIEW) {
                    validationErrors.addError(SET_COLUMN_REMARKS_NOT_SUPPORTED_ON_VIEW_MSG);
                }
            } else {
                // Check if we're trying to set the column remarks on a view, and if so, note that this is not supported.
                try {
                    List<Map<String, ?>> viewList = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database).queryForList(
                            new RawParameterizedSqlStatement(String.format("SHOW VIEWS LIKE %s", statement.getTableName())));
                    if (!viewList.isEmpty()) {
                        validationErrors.addError(SET_COLUMN_REMARKS_NOT_SUPPORTED_ON_VIEW_MSG);
                    }
                } catch (DatabaseException e) {
                    Scope.getCurrentScope().getLog(getClass()).severe("Failed to query Snowflake to determine if object is a table or view.", e);
                }
            }
        }
        return validationErrors;
    }
}

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
import liquibase.structure.core.Catalog;
import liquibase.structure.core.Schema;
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
                            buildShowViewsStatement(statement, database));
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

    /**
     * Builds the SHOW VIEWS used to tell a table apart from a view.
     * <p>
     * The table name is a LIKE pattern, so {@code %} and {@code _} have to be escaped or a table
     * named MY_TABLE also matches a view named MYXTABLE. The statement is also scoped to the
     * schema being changed: an unscoped SHOW VIEWS searches every database the role can see, so a
     * same-named view in an unrelated schema would make this look like a view.
     */
    protected RawParameterizedSqlStatement buildShowViewsStatement(SetColumnRemarksStatement statement, Database database) {
        String pattern = database.escapeStringForDatabase(statement.getTableName())
                .replace("%", "\\%")
                .replace("_", "\\_");
        StringBuilder sql = new StringBuilder(String.format("SHOW VIEWS LIKE '%s'", pattern));

        String schemaName = statement.getSchemaName() != null ? statement.getSchemaName() : database.getDefaultSchemaName();
        if (schemaName != null) {
            String catalogName = statement.getCatalogName() != null ? statement.getCatalogName() : database.getDefaultCatalogName();
            sql.append(" IN SCHEMA ");
            if (catalogName != null) {
                sql.append(database.escapeObjectName(catalogName, Catalog.class)).append('.');
            }
            sql.append(database.escapeObjectName(schemaName, Schema.class));
        }

        return new RawParameterizedSqlStatement(sql.toString());
    }
}

package liquibase.actionlogic.core.mssql;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.ExecuteSqlAction;
import liquibase.action.core.ColumnDefinition;
import liquibase.action.core.CreateTableAction;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.core.CreateTableLogic;
import liquibase.database.Database;
import liquibase.database.core.mssql.MSSQLDatabase;

import java.util.List;

public class CreateTableLogicMSSQL extends CreateTableLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return MSSQLDatabase.class;
    }

    @Override
    protected StringClauses generateSql(Action action, Scope scope) {
        StringClauses clauses = super.generateSql(action, scope);

        String tablespace = action.get(CreateTableAction.Attr.tablespace, String.class);
        if (tablespace != null) {
            clauses.replace(Clauses.tablespace, "ON "+tablespace);
        }

        return clauses;
    }

    @Override
    protected StringClauses generateColumnSql(ColumnDefinition column, Action action, Scope scope, List<Action> additionalActions) {
        MSSQLDatabase database = scope.get(Scope.Attr.database, MSSQLDatabase.class);
        StringClauses clauses = super.generateColumnSql(column, action, scope, additionalActions);

        String defaultValue = clauses.get(ColumnClauses.defaultValue);
        if (defaultValue != null) {
            clauses.replace(ColumnClauses.defaultValue, defaultValue.replaceFirst("DEFAULT", "CONSTRAINT " + database.generateDefaultConstraintName(action.get(CreateTableAction.Attr.tableName, String.class), column.get(ColumnDefinition.Attr.columnName, String.class))));
        }

        String remarks = column.get(ColumnDefinition.Attr.remarks, String.class);

        if (remarks != null) {
            String schemaName = action.get(CreateTableAction.Attr.schemaName, String.class);
            if (schemaName == null) {
                schemaName = database.getDefaultSchemaName();
            }
            additionalActions.add(new ExecuteSqlAction("EXEC sp_addextendedproperty @name = N'MS_Description', @value = '"+remarks+"', @level0type = N'Schema', @level0name = "+ schemaName +", @level1type = N'Table', @level1name = "+action.get(CreateTableAction.Attr.tableName, String.class)+", @level2type = N'Column', @level2name = "+column));
        }

        return clauses;
    }
}

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
import liquibase.structure.ObjectName;

import java.util.List;

public class CreateTableLogicMSSQL extends CreateTableLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return MSSQLDatabase.class;
    }

    @Override
    protected StringClauses generateSql(CreateTableAction action, Scope scope) {
        StringClauses clauses = super.generateSql(action, scope);

        String tablespace = action.tablespace;
        if (tablespace != null) {
            clauses.replace(Clauses.tablespace, "ON "+tablespace);
        }

        return clauses;
    }

    @Override
    protected StringClauses generateColumnSql(ColumnDefinition column, CreateTableAction action, Scope scope, List<Action> additionalActions) {
        MSSQLDatabase database = scope.get(Scope.Attr.database, MSSQLDatabase.class);
        StringClauses clauses = super.generateColumnSql(column, action, scope, additionalActions);

        String defaultValue = clauses.get(ColumnClauses.defaultValue);
        if (defaultValue != null) {
            clauses.replace(ColumnClauses.defaultValue, defaultValue.replaceFirst("DEFAULT", "CONSTRAINT " + database.generateDefaultConstraintName(column.columnName)));
        }

        String remarks = column.remarks;

        if (remarks != null) {
            String schemaName = action.tableName.container.name;
            if (schemaName == null) {
                schemaName = database.getDefaultSchemaName();
            }
            additionalActions.add(new ExecuteSqlAction("EXEC sp_addextendedproperty @name = N'MS_Description', @value = '"+remarks+"', @level0type = N'Schema', @level0name = "+ schemaName +", @level1type = N'Table', @level1name = "+action.tableName+", @level2type = N'Column', @level2name = "+column));
        }

        return clauses;
    }
}

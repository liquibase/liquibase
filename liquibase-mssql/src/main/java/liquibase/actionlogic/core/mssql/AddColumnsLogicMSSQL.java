package liquibase.actionlogic.core.mssql;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.AddColumnsAction;
import liquibase.action.core.ColumnDefinition;
import liquibase.actionlogic.core.AddColumnsLogic;
import liquibase.database.Database;
import liquibase.database.core.mssql.MSSQLDatabase;
import liquibase.datatype.DataTypeFactory;
import liquibase.structure.ObjectName;

public class AddColumnsLogicMSSQL extends AddColumnsLogic {

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return MSSQLDatabase.class;
    }

    @Override
    protected String getDefaultValueClause(ColumnDefinition column, Action action, Scope scope) {
        MSSQLDatabase database = scope.get(Scope.Attr.database, MSSQLDatabase.class);
        String clause = super.getDefaultValueClause(column, action, scope);

        if (clause == null) {
            return null;
        } else {
            return "CONSTRAINT "
                    + database.generateDefaultConstraintName(column.get(ColumnDefinition.Attr.columnName, ObjectName.class))
                    + clause;
        }


    }
}

package liquibase.actionlogic.core.mssql;

import liquibase.Scope;
import liquibase.action.core.AddColumnsAction;
import liquibase.actionlogic.core.AddColumnsLogic;
import liquibase.database.Database;
import liquibase.database.core.mssql.MSSQLDatabase;
import liquibase.structure.core.Column;

public class AddColumnsLogicMSSQL extends AddColumnsLogic {

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return MSSQLDatabase.class;
    }

    @Override
    protected String getDefaultValueClause(Column column, AddColumnsAction action, Scope scope) {
        MSSQLDatabase database = (MSSQLDatabase) scope.getDatabase();
        String clause = super.getDefaultValueClause(column, action, scope);

        if (clause == null) {
            return null;
        } else {
            return "CONSTRAINT "
                    + database.generateDefaultConstraintName(column.name)
                    + clause;
        }


    }
}

package liquibase.actionlogic.core.mssql;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.SetTableRemarksAction;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.core.SetTableRemarksLogic;
import liquibase.database.Database;
import liquibase.database.core.mssql.MSSQLDatabase;
import liquibase.structure.ObjectName;
import liquibase.structure.core.Table;

public class SetTableRemarksLogicMSSQL extends SetTableRemarksLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return MSSQLDatabase.class;
    }

    @Override
    protected StringClauses generateSql(SetTableRemarksAction action, Scope scope) {
        Database database = scope.getDatabase();

        return new StringClauses().append("ALTER TABLE")
                .append(database.escapeObjectName(action.tableName, Table.class))
                .append("COMMENT = '" + database.escapeStringForDatabase(action.remarks) + "'");
    }
}

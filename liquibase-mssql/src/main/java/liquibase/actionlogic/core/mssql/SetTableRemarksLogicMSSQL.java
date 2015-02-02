package liquibase.actionlogic.core.mssql;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.SetTableRemarksAction;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.core.SetTableRemarksLogic;
import liquibase.database.Database;
import liquibase.database.core.mssql.MSSQLDatabase;

public class SetTableRemarksLogicMSSQL extends SetTableRemarksLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return MSSQLDatabase.class;
    }

    @Override
    protected StringClauses generateSql(Action action, Scope scope) {
        Database database = scope.get(Scope.Attr.database, Database.class);

        return new StringClauses().append("ALTER TABLE")
                .append(database.escapeTableName(
                        action.get(SetTableRemarksAction.Attr.catalogName, String.class),
                        action.get(SetTableRemarksAction.Attr.schemaName, String.class),
                        action.get(SetTableRemarksAction.Attr.tableName, String.class)))
                .append("COMMENT = '" + database.escapeStringForDatabase(action.get(SetTableRemarksAction.Attr.remarks, String.class)) + "'");
    }
}

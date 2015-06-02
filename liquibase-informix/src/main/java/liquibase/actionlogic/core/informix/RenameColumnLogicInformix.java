package liquibase.actionlogic.core.informix;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.RenameColumnAction;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.core.RenameColumnLogic;
import liquibase.database.Database;
import liquibase.database.core.informix.InformixDatabase;
import liquibase.structure.ObjectName;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;

public class RenameColumnLogicInformix extends RenameColumnLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return InformixDatabase.class;
    }

    @Override
    protected StringClauses generateSql(Action action, Scope scope) {
        Database database = scope.get(Scope.Attr.database, Database.class);
        return new StringClauses()
                .append("RENAME COLUMN")
                .append(database.escapeObjectName(action.get(RenameColumnAction.Attr.tableName, ObjectName.class), Table.class)
                        + "."
                        + database.escapeObjectName(action.get(RenameColumnAction.Attr.oldColumnName, String.class), Column.class))
                .append("TO")
                .append(database.escapeObjectName(action.get(RenameColumnAction.Attr.newColumnName, String.class), Column.class));
    }
}

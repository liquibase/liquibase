package liquibase.actionlogic.core.informix;

import liquibase.Scope;
import liquibase.action.core.RenameColumnAction;
import liquibase.actionlogic.core.RenameColumnLogic;
import liquibase.database.Database;
import liquibase.database.core.informix.InformixDatabase;
import liquibase.structure.core.Column;
import liquibase.util.StringClauses;

public class RenameColumnLogicInformix extends RenameColumnLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return InformixDatabase.class;
    }

    @Override
    protected StringClauses generateSql(RenameColumnAction action, Scope scope) {
        Database database = scope.getDatabase();
        return new StringClauses()
                .append("RENAME COLUMN")
                .append(database.escapeObjectName(action.tableName)
                        + "."
                        + database.escapeObjectName(action.oldColumnName, Column.class))
                .append("TO")
                .append(database.escapeObjectName(action.newColumnName, Column.class));
    }
}

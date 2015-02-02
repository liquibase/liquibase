package liquibase.actionlogic.core.derby;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.RenameColumnAction;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.core.RenameColumnLogic;
import liquibase.database.Database;
import liquibase.database.core.derby.DerbyDatabase;
import liquibase.structure.core.Column;

public class RenameColumnLogicDerby extends RenameColumnLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return DerbyDatabase.class;
    }

    @Override
    protected StringClauses generateSql(Action action, Scope scope) {
        Database database = scope.get(Scope.Attr.database, Database.class);
        return new StringClauses()
                .append("RENAME COLUMN")
                .append(database.escapeTableName(
                        action.get(RenameColumnAction.Attr.catalogName, String.class),
                        action.get(RenameColumnAction.Attr.schemaName, String.class),
                        action.get(RenameColumnAction.Attr.tableName, String.class))
                        + "."
                        + database.escapeObjectName(action.get(RenameColumnAction.Attr.oldColumnName, String.class), Column.class))
                .append("TO")
                .append(database.escapeObjectName(action.get(RenameColumnAction.Attr.newColumnName, String.class), Column.class));
    }
}

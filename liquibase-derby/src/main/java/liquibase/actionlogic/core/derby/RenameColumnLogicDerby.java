package liquibase.actionlogic.core.derby;

import liquibase.Scope;
import liquibase.action.core.RenameColumnAction;
import liquibase.actionlogic.core.RenameColumnLogic;
import liquibase.database.Database;
import liquibase.database.core.derby.DerbyDatabase;
import liquibase.structure.core.Column;
import liquibase.util.StringClauses;

public class RenameColumnLogicDerby extends RenameColumnLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return DerbyDatabase.class;
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

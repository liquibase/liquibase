package liquibase.actionlogic.core.informix;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.ExecuteSqlAction;
import liquibase.action.core.AddLookupTableAction;
import liquibase.actionlogic.core.AddLookupTableLogic;
import liquibase.database.Database;
import liquibase.database.core.informix.InformixDatabase;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;

public class AddLookupTableLogicInformix extends AddLookupTableLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return InformixDatabase.class;
    }

    @Override
    public Action[] generateCreateAndLoadActions(AddLookupTableAction action, Scope scope) {
        Database database = scope.getDatabase();
        return new Action[]{
                new ExecuteSqlAction("CREATE TABLE "
                        + database.escapeObjectName(action.newColumnName.container, Table.class)
                        + " ( "
                        + database.escapeObjectName(action.newColumnName, Column.class)
                        + " "
                        + action.newColumnDataType
                        + " )"),
                new ExecuteSqlAction("INSERT INTO "
                        + database.escapeObjectName(action.newColumnName.container, Table.class)
                        + " ( "
                        + action.newColumnName
                        + " ) SELECT DISTINCT "
                        + action.existingColumnName
                        + " FROM "
                        + database.escapeObjectName(action.existingColumnName.container, Table.class)
                        + " WHERE "
                        + action.existingColumnName.name
                        + " IS NOT NULL"),
        };
    }
}

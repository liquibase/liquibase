package liquibase.actionlogic.core.informix;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.ExecuteSqlAction;
import liquibase.action.core.AddLookupTableAction;
import liquibase.actionlogic.core.AddLookupTableLogic;
import liquibase.database.Database;
import liquibase.database.core.informix.InformixDatabase;
import liquibase.structure.core.Column;

public class AddLookupTableLogicInformix extends AddLookupTableLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return InformixDatabase.class;
    }

    @Override
    public Action[] generateCreateAndLoadActions(Action action, Scope scope) {
        Database database = scope.get(Scope.Attr.database, Database.class);
        return new Action[]{
                new ExecuteSqlAction("CREATE TABLE "
                        + database.escapeTableName(action.get(AddLookupTableAction.Attr.newTableCatalogName, String.class), action.get(AddLookupTableAction.Attr.newTableSchemaName, String.class), action.get(AddLookupTableAction.Attr.newTableName, String.class))
                        + " ( "
                        + database.escapeObjectName(action.get(AddLookupTableAction.Attr.newColumnName, String.class), Column.class)
                        + " "
                        + action.get(AddLookupTableAction.Attr.newColumnDataType, String.class)
                        + " )"),
                new ExecuteSqlAction("INSERT INTO "
                        + database.escapeTableName(action.get(AddLookupTableAction.Attr.newTableCatalogName, String.class), action.get(AddLookupTableAction.Attr.newTableSchemaName, String.class), action.get(AddLookupTableAction.Attr.newTableName, String.class))
                        + " ( "
                        + action.get(AddLookupTableAction.Attr.newColumnName, String.class)
                        + " ) SELECT DISTINCT "
                        + action.get(AddLookupTableAction.Attr.existingColumnName, String.class)
                        + " FROM "
                        + database.escapeTableName(action.get(AddLookupTableAction.Attr.existingTableCatalogName, String.class), action.get(AddLookupTableAction.Attr.existingTableSchemaName, String.class), action.get(AddLookupTableAction.Attr.existingTableName, String.class))
                        + " WHERE "
                        + action.get(AddLookupTableAction.Attr.existingColumnName, String.class)
                        + " IS NOT NULL"),
        };
    }
}

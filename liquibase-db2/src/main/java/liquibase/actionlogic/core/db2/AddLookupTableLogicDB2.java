package liquibase.actionlogic.core.db2;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.ExecuteSqlAction;
import liquibase.action.UpdateSqlAction;
import liquibase.action.core.AddLookupTableAction;
import liquibase.actionlogic.core.AddLookupTableLogic;
import liquibase.database.Database;
import liquibase.database.core.db2.DB2Database;
import liquibase.structure.core.Column;

public class AddLookupTableLogicDB2 extends AddLookupTableLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return DB2Database.class;
    }

    @Override
    public Action[] generateCreateAndLoadActions(Action action, Scope scope) {
        Database database = scope.get(Scope.Attr.database, Database.class);
        return new Action[]{
                new ExecuteSqlAction("CREATE TABLE "
                        + database.escapeTableName(action.get(AddLookupTableAction.Attr.newTableCatalogName, String.class), action.get(AddLookupTableAction.Attr.newTableSchemaName, String.class), action.get(AddLookupTableAction.Attr.newTableName, String.class))
                        + " AS (SELECT "
                        + database.escapeObjectName(action.get(AddLookupTableAction.Attr.existingColumnName, String.class), Column.class)
                        + " AS "
                        + database.escapeObjectName(action.get(AddLookupTableAction.Attr.newColumnName, String.class), Column.class)
                        + " FROM "
                        + database.escapeTableName(action.get(AddLookupTableAction.Attr.existingTableCatalogName, String.class), action.get(AddLookupTableAction.Attr.existingTableSchemaName, String.class), action.get(AddLookupTableAction.Attr.existingTableName, String.class))
                        + ") WITH NO DATA"),
                new UpdateSqlAction("INSERT INTO "
                        + database.escapeTableName(action.get(AddLookupTableAction.Attr.newTableCatalogName, String.class), action.get(AddLookupTableAction.Attr.newTableSchemaName, String.class), action.get(AddLookupTableAction.Attr.newTableName, String.class))
                        + " SELECT DISTINCT "
                        + database.escapeObjectName(action.get(AddLookupTableAction.Attr.existingColumnName, String.class), Column.class)
                        + " FROM "
                        + database.escapeTableName(action.get(AddLookupTableAction.Attr.existingTableCatalogName, String.class), action.get(AddLookupTableAction.Attr.existingTableSchemaName, String.class), action.get(AddLookupTableAction.Attr.existingTableName, String.class))
                        + " WHERE "
                        + database.escapeObjectName(action.get(AddLookupTableAction.Attr.existingColumnName, String.class), Column.class)
                        + " IS NOT NULL"),
        };
    }
}

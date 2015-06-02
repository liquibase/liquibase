package liquibase.actionlogic.core.db2;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.ExecuteSqlAction;
import liquibase.action.UpdateSqlAction;
import liquibase.action.core.AddLookupTableAction;
import liquibase.actionlogic.core.AddLookupTableLogic;
import liquibase.database.Database;
import liquibase.database.core.db2.DB2Database;
import liquibase.structure.ObjectName;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;

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
                        + database.escapeObjectName(action.get(AddLookupTableAction.Attr.newColumnName, ObjectName.class).getContainer(), Table.class)
                        + " AS (SELECT "
                        + database.escapeObjectName(action.get(AddLookupTableAction.Attr.existingColumnName, String.class), Column.class)
                        + " AS "
                        + database.escapeObjectName(action.get(AddLookupTableAction.Attr.newColumnName, String.class), Column.class)
                        + " FROM "
                        + database.escapeObjectName(action.get(AddLookupTableAction.Attr.existingColumnName, ObjectName.class).getContainer(), Table.class)
                        + ") WITH NO DATA"),
                new UpdateSqlAction("INSERT INTO "
                        + database.escapeObjectName(action.get(AddLookupTableAction.Attr.newColumnName, ObjectName.class).getContainer(), Table.class)
                        + " SELECT DISTINCT "
                        + database.escapeObjectName(action.get(AddLookupTableAction.Attr.existingColumnName, String.class), Column.class)
                        + " FROM "
                        + database.escapeObjectName(action.get(AddLookupTableAction.Attr.existingColumnName, ObjectName.class), Table.class)
                        + " WHERE "
                        + database.escapeObjectName(action.get(AddLookupTableAction.Attr.existingColumnName, String.class), Column.class)
                        + " IS NOT NULL"),
        };
    }
}

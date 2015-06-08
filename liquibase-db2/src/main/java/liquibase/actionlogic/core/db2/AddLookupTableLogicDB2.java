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
    public Action[] generateCreateAndLoadActions(AddLookupTableAction action, Scope scope) {
        Database database = scope.getDatabase();
        return new Action[]{
                new ExecuteSqlAction("CREATE TABLE "
                        + database.escapeObjectName(action.newColumnName.container, Table.class)
                        + " AS (SELECT "
                        + database.escapeObjectName(action.existingColumnName, Column.class)
                        + " AS "
                        + database.escapeObjectName(action.newColumnName, Column.class)
                        + " FROM "
                        + database.escapeObjectName(action.existingColumnName.container, Table.class)
                        + ") WITH NO DATA"),
                new UpdateSqlAction("INSERT INTO "
                        + database.escapeObjectName(action.newColumnName.container, Table.class)
                        + " SELECT DISTINCT "
                        + database.escapeObjectName(action.existingColumnName, Column.class)
                        + " FROM "
                        + database.escapeObjectName(action.existingColumnName, Table.class)
                        + " WHERE "
                        + database.escapeObjectName(action.existingColumnName, Column.class)
                        + " IS NOT NULL"),
        };
    }
}

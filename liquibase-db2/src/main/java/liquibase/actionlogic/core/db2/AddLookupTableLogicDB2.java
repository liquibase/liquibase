package liquibase.actionlogic.core.db2;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.ExecuteSqlAction;
import liquibase.action.UpdateSqlAction;
import liquibase.action.core.AddLookupTableAction;
import liquibase.actionlogic.core.AddLookupTableLogic;
import liquibase.database.Database;
import liquibase.database.core.db2.DB2Database;

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
                        + database.escapeObjectName(action.newColumnName.container)
                        + " AS (SELECT "
                        + database.escapeObjectName(action.existingColumnName)
                        + " AS "
                        + database.escapeObjectName(action.newColumnName)
                        + " FROM "
                        + database.escapeObjectName(action.existingColumnName.container)
                        + ") WITH NO DATA"),
                new UpdateSqlAction("INSERT INTO "
                        + database.escapeObjectName(action.newColumnName.container)
                        + " SELECT DISTINCT "
                        + database.escapeObjectName(action.existingColumnName)
                        + " FROM "
                        + database.escapeObjectName(action.existingColumnName)
                        + " WHERE "
                        + database.escapeObjectName(action.existingColumnName)
                        + " IS NOT NULL"),
        };
    }
}

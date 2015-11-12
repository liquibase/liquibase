package liquibase.actionlogic.core.informix;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.ExecuteSqlAction;
import liquibase.action.core.AddLookupTableAction;
import liquibase.actionlogic.core.AddLookupTableLogic;
import liquibase.database.Database;
import liquibase.database.core.informix.InformixDatabase;

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
                        + database.escapeObjectName(action.newColumnName.container)
                        + " ( "
                        + database.escapeObjectName(action.newColumnName)
                        + " "
                        + action.newColumnDataType
                        + " )"),
                new ExecuteSqlAction("INSERT INTO "
                        + database.escapeObjectName(action.newColumnName.container)
                        + " ( "
                        + action.newColumnName
                        + " ) SELECT DISTINCT "
                        + action.existingColumnName
                        + " FROM "
                        + database.escapeObjectName(action.existingColumnName.container)
                        + " WHERE "
                        + action.existingColumnName.name
                        + " IS NOT NULL"),
        };
    }
}

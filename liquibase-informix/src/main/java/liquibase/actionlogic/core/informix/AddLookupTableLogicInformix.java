package liquibase.actionlogic.core.informix;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.ExecuteSqlAction;
import liquibase.action.core.AddLookupTableAction;
import liquibase.actionlogic.core.AddLookupTableLogic;
import liquibase.database.Database;
import liquibase.database.core.informix.InformixDatabase;
import liquibase.structure.ObjectName;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;

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
                        + database.escapeObjectName(action.get(AddLookupTableAction.Attr.newColumnName, ObjectName.class).getContainer(), Table.class)
                        + " ( "
                        + database.escapeObjectName(action.get(AddLookupTableAction.Attr.newColumnName, String.class), Column.class)
                        + " "
                        + action.get(AddLookupTableAction.Attr.newColumnDataType, String.class)
                        + " )"),
                new ExecuteSqlAction("INSERT INTO "
                        + database.escapeObjectName(action.get(AddLookupTableAction.Attr.newColumnName, ObjectName.class).getContainer(), Table.class)
                        + " ( "
                        + action.get(AddLookupTableAction.Attr.newColumnName, String.class)
                        + " ) SELECT DISTINCT "
                        + action.get(AddLookupTableAction.Attr.existingColumnName, String.class)
                        + " FROM "
                        + database.escapeObjectName(action.get(AddLookupTableAction.Attr.existingColumnName, ObjectName.class).getContainer(), Table.class)
                        + " WHERE "
                        + action.get(AddLookupTableAction.Attr.existingColumnName, String.class)
                        + " IS NOT NULL"),
        };
    }
}

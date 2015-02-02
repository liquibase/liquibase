package liquibase.actionlogic.core.derby;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.ExecuteSqlAction;
import liquibase.action.core.RenameTableAction;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.actionlogic.core.RenameTableLogic;
import liquibase.database.Database;
import liquibase.database.core.derby.DerbyDatabase;
import liquibase.exception.ActionPerformException;
import liquibase.structure.core.Table;

public class RenameTableLogicDerby extends RenameTableLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return DerbyDatabase.class;
    }


    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        Database database = scope.get(Scope.Attr.database, Database.class);
        return new DelegateResult(new ExecuteSqlAction(
                "RENAME TABLE "
                        + database.escapeTableName(action.get(RenameTableAction.Attr.catalogName, String.class),
                        action.get(RenameTableAction.Attr.schemaName, String.class),
                        action.get(RenameTableAction.Attr.oldTableName, String.class))
                        + " TO "
                        + database.escapeObjectName(action.get(RenameTableAction.Attr.catalogName, String.class), Table.class)
        ));
    }
}

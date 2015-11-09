package liquibase.actionlogic.core.informix;

import liquibase.Scope;
import liquibase.action.ExecuteSqlAction;
import liquibase.action.core.RenameTableAction;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.actionlogic.core.RenameTableLogic;
import liquibase.database.Database;
import liquibase.database.core.informix.InformixDatabase;
import liquibase.exception.ActionPerformException;
import liquibase.structure.core.Table;

public class RenameTableLogicInformix extends RenameTableLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return InformixDatabase.class;
    }


    @Override
    public ActionResult execute(RenameTableAction action, Scope scope) throws ActionPerformException {
        Database database = scope.getDatabase();
        return new DelegateResult(new ExecuteSqlAction(
                "RENAME TABLE "
                        + database.escapeObjectName(action.oldTableName, Table.class)
                        + " TO "
                        + database.escapeObjectName(action.newTableName.name, Table.class)
        ));
    }
}

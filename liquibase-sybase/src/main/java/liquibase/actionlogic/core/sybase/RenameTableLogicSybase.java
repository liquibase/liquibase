package liquibase.actionlogic.core.sybase;

import liquibase.Scope;
import liquibase.action.ExecuteSqlAction;
import liquibase.action.core.RenameTableAction;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.actionlogic.core.RenameTableLogic;
import liquibase.database.Database;
import liquibase.database.core.sybase.SybaseDatabase;
import liquibase.exception.ActionPerformException;
import liquibase.structure.core.Table;

public class RenameTableLogicSybase extends RenameTableLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return SybaseDatabase.class;
    }

    @Override
    public ActionResult execute(RenameTableAction action, Scope scope) throws ActionPerformException {
        Database database = scope.getDatabase();
        return new DelegateResult(new ExecuteSqlAction(
                "exec sp_rename '"
                        + database.escapeObjectName(action.oldTableName, Table.class)
                        + "', '"
                        + action.newTableName.name
                        + "'"));
    }
}

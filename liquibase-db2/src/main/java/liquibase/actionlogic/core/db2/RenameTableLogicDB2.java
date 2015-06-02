package liquibase.actionlogic.core.db2;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.ExecuteSqlAction;
import liquibase.action.core.RenameTableAction;
import liquibase.action.core.ReorganizeTableAction;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.actionlogic.core.RenameTableLogic;
import liquibase.database.Database;
import liquibase.database.core.db2.DB2Database;
import liquibase.exception.ActionPerformException;
import liquibase.structure.ObjectName;
import liquibase.structure.core.Table;

public class RenameTableLogicDB2 extends RenameTableLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return DB2Database.class;
    }


    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        Database database = scope.get(Scope.Attr.database, Database.class);
        return new DelegateResult(new ExecuteSqlAction(
                "RENAME TABLE "
                        + database.escapeObjectName(action.get(RenameTableAction.Attr.oldTableName, ObjectName.class), Table.class)
                        + " TO "
                        + database.escapeObjectName(action.get(RenameTableAction.Attr.newTableName, ObjectName.class), Table.class)
        ),
                new ReorganizeTableAction(action.get(RenameTableAction.Attr.newTableName, ObjectName.class)));
    }
}

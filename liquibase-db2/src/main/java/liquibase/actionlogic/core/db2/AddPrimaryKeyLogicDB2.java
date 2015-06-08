package liquibase.actionlogic.core.db2;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.AddPrimaryKeyAction;
import liquibase.action.core.ReorganizeTableAction;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.actionlogic.core.AddPrimaryKeyLogic;
import liquibase.database.Database;
import liquibase.database.core.db2.DB2Database;
import liquibase.exception.ActionPerformException;
import liquibase.structure.ObjectName;

public class AddPrimaryKeyLogicDB2 extends AddPrimaryKeyLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return DB2Database.class;
    }

    @Override
    public ActionResult execute(AddPrimaryKeyAction action, Scope scope) throws ActionPerformException {
        return new DelegateResult((DelegateResult) super.execute(action, scope),
                new ReorganizeTableAction(action.tableName));
    }
}

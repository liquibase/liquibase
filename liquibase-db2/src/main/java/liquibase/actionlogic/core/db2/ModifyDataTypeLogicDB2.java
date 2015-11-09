package liquibase.actionlogic.core.db2;

import liquibase.Scope;
import liquibase.action.core.ModifyDataTypeAction;
import liquibase.action.core.ReorganizeTableAction;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.actionlogic.core.ModifyDataTypeLogic;
import liquibase.database.Database;
import liquibase.database.core.db2.DB2Database;
import liquibase.exception.ActionPerformException;

public class ModifyDataTypeLogicDB2 extends ModifyDataTypeLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return DB2Database.class;
    }

    @Override
    public ActionResult execute(ModifyDataTypeAction action, Scope scope) throws ActionPerformException {
        return new DelegateResult(
                (DelegateResult) super.execute(action, scope),
                new ReorganizeTableAction(action.columnName.container));
    }
}

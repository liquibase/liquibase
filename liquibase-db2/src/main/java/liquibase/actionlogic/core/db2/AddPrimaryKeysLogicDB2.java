package liquibase.actionlogic.core.db2;

import liquibase.Scope;
import liquibase.action.core.AddPrimaryKeysAction;
import liquibase.action.core.ReorganizeTableAction;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.actionlogic.core.AddPrimaryKeysLogic;
import liquibase.database.Database;
import liquibase.database.core.db2.DB2Database;
import liquibase.exception.ActionPerformException;

public class AddPrimaryKeysLogicDB2 extends AddPrimaryKeysLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return DB2Database.class;
    }

//    @Override
//    public ActionResult execute(AddPrimaryKeysAction action, Scope scope) throws ActionPerformException {
//        return new DelegateResult((DelegateResult) super.execute(action, scope),
//                new ReorganizeTableAction(action.primaryKey.name.container));
//    }
}

package liquibase.actionlogic.core.db2;

import liquibase.Scope;
import liquibase.action.ExecuteSqlAction;
import liquibase.action.core.ReorganizeTableAction;
import liquibase.actionlogic.*;
import liquibase.database.Database;
import liquibase.database.core.db2.DB2Database;
import liquibase.exception.ActionPerformException;
import liquibase.exception.DatabaseException;
import liquibase.exception.ValidationErrors;

public class ReorganizeTableLogicDB2 extends AbstractActionLogic<ReorganizeTableAction> implements ActionLogic.InteractsExternally<ReorganizeTableAction> {

    @Override
    public boolean interactsExternally(ReorganizeTableAction action, Scope scope) {
        return true;
    }

    @Override
    protected Class<ReorganizeTableAction> getSupportedAction() {
        return ReorganizeTableAction.class;
    }

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return DB2Database.class;
    }

    @Override
    protected boolean supportsScope(Scope scope) {
        return super.supportsScope(scope) && !((DB2Database) scope.getDatabase()).isZOS();
    }

    @Override
    public ValidationErrors validate(ReorganizeTableAction action, Scope scope) {
        return super.validate(action, scope)
                .checkForRequiredField("tableName", action);
    }

    @Override
    public ActionResult execute(ReorganizeTableAction action, Scope scope) throws ActionPerformException {
        Database database = scope.getDatabase();
        try {
            if (database.getDatabaseMajorVersion() >= 9) {
                return new DelegateResult(new ExecuteSqlAction(
                        "CALL SYSPROC.ADMIN_CMD ('REORG TABLE "
                                + database.escapeObjectName(action.tableName)
                                + "')"));
            } else {
                return new NoOpResult();
            }
        } catch (DatabaseException e) {
            throw new ActionPerformException(e);
        }
    }
}

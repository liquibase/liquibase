package liquibase.actionlogic.core.db2;

import javafx.scene.control.Tab;
import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.ExecuteSqlAction;
import liquibase.action.core.ReorganizeTableAction;
import liquibase.actionlogic.*;
import liquibase.database.Database;
import liquibase.database.core.db2.DB2Database;
import liquibase.exception.ActionPerformException;
import liquibase.exception.DatabaseException;
import liquibase.exception.ValidationErrors;
import liquibase.structure.ObjectName;
import liquibase.structure.core.Table;

public class ReorganizeTableLogicDB2 extends AbstractActionLogic implements ActionLogic.InteractsExternally {

    @Override
    public boolean interactsExternally(Action action, Scope scope) {
        return true;
    }

    @Override
    protected Class<? extends Action> getSupportedAction() {
        return ReorganizeTableAction.class;
    }

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return DB2Database.class;
    }

    @Override
    public ValidationErrors validate(Action action, Scope scope) {
        return super.validate(action, scope)
                .checkForRequiredField(ReorganizeTableAction.Attr.tableName, action);
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        Database database = scope.get(Scope.Attr.database, Database.class);
        try {
            if (database.getDatabaseMajorVersion() >= 9) {
                return new DelegateResult(new ExecuteSqlAction(
                        "CALL SYSPROC.ADMIN_CMD ('REORG TABLE "
                                + database.escapeObjectName(action.get(ReorganizeTableAction.Attr.tableName, ObjectName.class), Table.class)
                                + "')"));
            } else {
                return new NoOpResult();
            }
        } catch (DatabaseException e) {
            throw new ActionPerformException(e);
        }
    }
}

package liquibase.actionlogic.core.sybase;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.ExecuteSqlAction;
import liquibase.action.core.RenameColumnAction;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.actionlogic.core.RenameColumnLogic;
import liquibase.exception.ActionPerformException;

public class RenameColumnLogicSybase extends RenameColumnLogic {

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        return new DelegateResult(new ExecuteSqlAction("exec sp_rename '"
                + action.get(RenameColumnAction.Attr.tableName, String.class)
                + "."
                + action.get(RenameColumnAction.Attr.oldColumnName, String.class)
                + "', '"
                + action.get(RenameColumnAction.Attr.newColumnName, String.class)
                + "'"));
    }
}

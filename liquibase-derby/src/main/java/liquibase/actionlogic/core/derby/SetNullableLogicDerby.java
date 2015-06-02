package liquibase.actionlogic.core.derby;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.ReorganizeTableAction;
import liquibase.action.core.SetNullableAction;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.actionlogic.core.SetNullableLogic;
import liquibase.exception.ActionPerformException;
import liquibase.structure.ObjectName;

public class SetNullableLogicDerby extends SetNullableLogic {
    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        return new DelegateResult(
                (DelegateResult) super.execute(action, scope),
                (ReorganizeTableAction) new ReorganizeTableAction(
                        action.get(SetNullableAction.Attr.columnName, ObjectName.class).getContainer()
                ));
    }
}

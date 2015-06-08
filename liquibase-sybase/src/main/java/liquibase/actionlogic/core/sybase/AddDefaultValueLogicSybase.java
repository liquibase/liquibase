package liquibase.actionlogic.core.sybase;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.AddDefaultValueAction;
import liquibase.action.core.AlterTableAction;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.actionlogic.core.AddDefaultValueLogic;
import liquibase.database.Database;
import liquibase.database.core.sybase.SybaseDatabase;
import liquibase.datatype.DataTypeFactory;
import liquibase.exception.ActionPerformException;
import liquibase.structure.ObjectName;
import liquibase.structure.core.Column;

public class AddDefaultValueLogicSybase extends AddDefaultValueLogic {

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return SybaseDatabase.class;
    }

    @Override
    public ActionResult execute(AddDefaultValueAction action, Scope scope) throws ActionPerformException {
        Database database = scope.getDatabase();
        Object defaultValue = action.defaultValue;

        return new DelegateResult(new AlterTableAction(
                action.columnName.container,
                new StringClauses()
                        .append("REPLACE")
                        .append(database.escapeObjectName(action.columnName, Column.class))
                        .append("DEFAULT")
                        .append(DataTypeFactory.getInstance().fromObject(defaultValue, database).objectToSql(defaultValue, database))
        ));
    }
}
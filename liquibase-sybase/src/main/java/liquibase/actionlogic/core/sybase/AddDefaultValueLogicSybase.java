package liquibase.actionlogic.core.sybase;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.AddDefaultValueAction;
import liquibase.action.core.RedefineTableAction;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.RewriteResult;
import liquibase.actionlogic.core.AddDefaultValueLogic;
import liquibase.database.Database;
import liquibase.database.core.sybase.SybaseDatabase;
import liquibase.datatype.DataTypeFactory;
import liquibase.exception.ActionPerformException;
import liquibase.structure.core.Column;

public class AddDefaultValueLogicSybase extends AddDefaultValueLogic {

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return SybaseDatabase.class;
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        Database database = scope.get(Scope.Attr.database, Database.class);
        Object defaultValue = action.get(AddDefaultValueAction.Attr.defaultValue, Object.class);

        return new RewriteResult(new RedefineTableAction(
                action.get(AddDefaultValueAction.Attr.catalogName, String.class),
                action.get(AddDefaultValueAction.Attr.schemaName, String.class),
                action.get(AddDefaultValueAction.Attr.tableName, String.class),
                "REPLACE " + database.escapeObjectName(action.get(AddDefaultValueAction.Attr.columnName, String.class), Column.class) + " DEFAULT " + DataTypeFactory.getInstance().fromObject(defaultValue, database).objectToSql(defaultValue, database)
        ));
    }
}
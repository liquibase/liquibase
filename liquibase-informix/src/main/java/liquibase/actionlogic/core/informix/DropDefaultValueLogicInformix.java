package liquibase.actionlogic.core.informix;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.ExecuteSqlAction;
import liquibase.action.core.DropDefaultValueAction;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.RewriteResult;
import liquibase.actionlogic.core.DropDefaultValueLogic;
import liquibase.database.Database;
import liquibase.database.core.informix.InformixDatabase;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;
import liquibase.structure.core.Column;

public class DropDefaultValueLogicInformix extends DropDefaultValueLogic {

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return InformixDatabase.class;
    }

    @Override
    public ValidationErrors validate(Action action, Scope scope) {
        return super.validate(action, scope)
                .checkForRequiredField(DropDefaultValueAction.Attr.columnDataType, action);
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        Database database = scope.get(Scope.Attr.database, Database.class);

        String escapedTableName = database.escapeTableName(action.get(DropDefaultValueAction.Attr.catalogName, String.class),
                action.get(DropDefaultValueAction.Attr.schemaName, String.class),
                action.get(DropDefaultValueAction.Attr.tableName, String.class));

        String columnName = action.get(DropDefaultValueAction.Attr.columnName, String.class);

        /*
         * TODO If dropped from a not null column the not null constraint will be dropped, too.
         * If the column is "NOT NULL" it has to be added behind the datatype.
         */
        return new RewriteResult(new ExecuteSqlAction("ALTER TABLE " + escapedTableName + " MODIFY (" + database.escapeObjectName(columnName, Column.class) + " " + action.get(DropDefaultValueAction.Attr.columnDataType, String.class) + ")"));

    }

    @Override
    protected StringClauses generateSql(Action action, Scope scope) {
        return new StringClauses().append("DROP DEFAULT");
    }
}

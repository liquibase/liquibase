package liquibase.actionlogic.core.mysql;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.RenameColumnAction;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.core.RenameColumnLogic;
import liquibase.database.Database;
import liquibase.database.core.mysql.MySQLDatabase;
import liquibase.datatype.DataTypeFactory;
import liquibase.exception.ValidationErrors;

public class RenameColumnLogicMysql extends RenameColumnLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return MySQLDatabase.class;
    }

    @Override
    public ValidationErrors validate(Action action, Scope scope) {
        return super.validate(action, scope)
                .checkForRequiredField(RenameColumnAction.Attr.columnDataType, action);
    }

    @Override
    protected StringClauses generateSql(Action action, Scope scope) {
        Database database = scope.get(Scope.Attr.database, Database.class);

        StringClauses clauses = super.generateSql(action, scope)
                .replace("RENAME COLUMN", "CHANGE")
                .remove("TO")
                .append(DataTypeFactory.getInstance().fromDescription(action.get(RenameColumnAction.Attr.columnDataType, String.class), database).toDatabaseDataType(database).toSql());

        String remarks = action.get(RenameColumnAction.Attr.remarks, String.class);
        if (remarks != null) {
            clauses.append("COMMENT '" + database.escapeStringForDatabase(remarks) + "'");
        }

        return clauses;
    }
}

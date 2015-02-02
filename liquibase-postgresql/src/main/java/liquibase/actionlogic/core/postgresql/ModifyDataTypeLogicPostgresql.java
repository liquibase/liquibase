package liquibase.actionlogic.core.postgresql;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.ModifyDataTypeAction;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.core.ModifyDataTypeLogic;
import liquibase.database.Database;
import liquibase.database.core.postgresql.PostgresDatabase;

public class ModifyDataTypeLogicPostgresql extends ModifyDataTypeLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return PostgresDatabase.class;
    }

    @Override
    protected StringClauses generateSql(Action action, Scope scope) {
        StringClauses clauses = super.generateSql(action, scope);

        clauses.prepend("TYPE");
        clauses.append("USING ("
                +action.get(ModifyDataTypeAction.Attr.columnName, String.class)
                +"::"
                +action.get(ModifyDataTypeAction.Attr.newDataType, String.class)
                +")");

        return clauses;
    }
}

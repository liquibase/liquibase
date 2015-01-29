package liquibase.actionlogic.core.oracle;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.ColumnDefinition;
import liquibase.action.core.CreateTableAction;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.core.CreateTableLogic;
import liquibase.database.Database;
import liquibase.database.core.oracle.OracleDatabase;

import java.util.List;

public class CreateTableLogicOracle extends CreateTableLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return OracleDatabase.class;
    }

    @Override
    protected StringClauses generateSql(Action action, Scope scope) {
        StringClauses clauses = super.generateSql(action, scope);

        String primaryKeyDef = clauses.get(Clauses.primaryKey);
        if (primaryKeyDef != null) {
            String primaryKeyTablespace = action.get(CreateTableAction.Attr.primaryKeyTablespace, String.class);
            if (primaryKeyTablespace != null) {
                clauses.append("USING INDEX TABLESPACE "+primaryKeyTablespace);
            }
        }

        return clauses;
    }

    @Override
    protected StringClauses generateColumnSql(ColumnDefinition column, Action action, Scope scope, List<Action> additionalActions) {
        StringClauses clauses = super.generateColumnSql(column, action, scope, additionalActions);

        String defaultValue = clauses.get(ColumnClauses.defaultValue);
        if (defaultValue != null && defaultValue.startsWith("GENERATED ALWAYS ")) {
            clauses.replace(ColumnClauses.defaultValue, defaultValue.replaceFirst("DEFAULT ", ""));
        }

        return clauses;
    }
}

package liquibase.actionlogic.core.asany;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.core.AddPrimaryKeyLogic;
import liquibase.database.Database;
import liquibase.database.core.asany.SybaseASADatabase;

public class AddPrimaryKeyLogicSybaseASA extends AddPrimaryKeyLogic {

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return SybaseASADatabase.class;
    }

    @Override
    protected StringClauses getAlterTableClauses(Action action, Scope scope) {
        StringClauses clauses = super.getAlterTableClauses(action, scope);
        clauses.replace("ADD CONSTRAINT", "ADD PRIMARY KEY");
        clauses.remove(Clauses.constraintName);

        return clauses;
    }
}

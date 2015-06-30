package liquibase.actionlogic.core.asany;

import liquibase.Scope;
import liquibase.action.core.AddPrimaryKeyAction;
import liquibase.actionlogic.core.AddPrimaryKeyLogic;
import liquibase.database.Database;
import liquibase.database.core.asany.SybaseASADatabase;
import liquibase.util.StringClauses;

public class AddPrimaryKeyLogicSybaseASA extends AddPrimaryKeyLogic {

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return SybaseASADatabase.class;
    }

    @Override
    protected StringClauses generateSql(AddPrimaryKeyAction action, Scope scope) {
        StringClauses clauses = super.generateSql(action, scope);
        clauses.replace("ADD CONSTRAINT", "ADD PRIMARY KEY");
        clauses.remove(Clauses.constraintName);

        return clauses;
    }
}

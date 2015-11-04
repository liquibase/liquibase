package liquibase.actionlogic.core.asany;

import liquibase.Scope;
import liquibase.action.core.AddPrimaryKeysAction;
import liquibase.actionlogic.core.AddPrimaryKeysLogic;
import liquibase.database.Database;
import liquibase.database.core.asany.SybaseASADatabase;
import liquibase.structure.core.PrimaryKey;
import liquibase.util.StringClauses;

public class AddPrimaryKeysLogicSybaseASA extends AddPrimaryKeysLogic {

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return SybaseASADatabase.class;
    }

    @Override
    protected StringClauses generateSql(PrimaryKey pk, AddPrimaryKeysAction action, Scope scope) {
        StringClauses clauses = super.generateSql(pk, action, scope);
        clauses.replace("ADD CONSTRAINT", "ADD PRIMARY KEY");
        clauses.remove(Clauses.constraintName);

        return clauses;
    }
}

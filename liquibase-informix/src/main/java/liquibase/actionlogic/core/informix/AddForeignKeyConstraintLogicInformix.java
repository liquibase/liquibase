package liquibase.actionlogic.core.informix;

import liquibase.Scope;
import liquibase.action.core.AddForeignKeyConstraintAction;
import liquibase.actionlogic.core.AddForeignKeyConstraintLogic;
import liquibase.database.Database;
import liquibase.database.core.informix.InformixDatabase;
import liquibase.util.StringClauses;

public class AddForeignKeyConstraintLogicInformix extends AddForeignKeyConstraintLogic {

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return InformixDatabase.class;
    }

    @Override
    protected StringClauses generateSql(AddForeignKeyConstraintAction action, Scope scope)  {
        StringClauses clauses = super.generateSql(action, scope)
                .remove(Clauses.constraintName)
                .remove("ON UPDATE")
                .remove("ON DELETE CASCADE");

        clauses.append("CONSTRAINT "+action.foreignKey.name);

        return clauses;
    }
}

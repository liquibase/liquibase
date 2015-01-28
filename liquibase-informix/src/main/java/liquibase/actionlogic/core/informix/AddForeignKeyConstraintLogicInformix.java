package liquibase.actionlogic.core.informix;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.AddForeignKeyConstraintAction;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.core.AddForeignKeyConstraintLogic;
import liquibase.database.Database;
import liquibase.database.core.informix.InformixDatabase;
import liquibase.exception.ActionPerformException;

public class AddForeignKeyConstraintLogicInformix extends AddForeignKeyConstraintLogic {

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return InformixDatabase.class;
    }

    @Override
    public StringClauses getAlterTableClauses(Action action, Scope scope) throws ActionPerformException {
        StringClauses clauses = super.getAlterTableClauses(action, scope)
                .remove(Clauses.constraintName)
                .remove("ON UPDATE")
                .remove("ON DELETE CASCADE");

        clauses.append("CONSTRAINT "+action.get(AddForeignKeyConstraintAction.Attr.constraintName, String.class));

        return clauses;
    }
}

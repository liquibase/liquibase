package liquibase.actionlogic.core.mysql;

import liquibase.Scope;
import liquibase.action.core.AddForeignKeysAction;
import liquibase.actionlogic.core.AddForeignKeysLogic;
import liquibase.database.Database;
import liquibase.database.core.mysql.MySQLDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.structure.core.ForeignKey;
import liquibase.structure.core.ForeignKeyConstraintType;

public class AddForeignKeysLogicMysql extends AddForeignKeysLogic {

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return MySQLDatabase.class;
    }

    @Override
    public ValidationErrors validate(AddForeignKeysAction action, Scope scope) {
        ValidationErrors errors = super.validate(action, scope);
        for (ForeignKey fk : action.foreignKeys) {
            if (fk.updateRule == ForeignKeyConstraintType.importedKeySetDefault) {
                errors.addUnsupportedError("Update rule SET DEFAULT", scope.getDatabase().getShortName());
            }

            if (fk.deleteRule == ForeignKeyConstraintType.importedKeySetDefault) {
                errors.addUnsupportedError("Delete rule SET DEFAULT", scope.getDatabase().getShortName());
            }
        }
        return errors;
    }
}

package liquibase.actionlogic.core.mysql;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.ModifyDataTypeAction;
import liquibase.actionlogic.core.ModifyDataTypeLogic;
import liquibase.database.Database;
import liquibase.database.core.mysql.MySQLDatabase;
import liquibase.exception.ValidationErrors;

public class ModifyDataTypeLogicMysql extends ModifyDataTypeLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return MySQLDatabase.class;
    }

    @Override
    public ValidationErrors validate(ModifyDataTypeAction action, Scope scope) {
        ValidationErrors errors = super.validate(action, scope);

        if (!errors.hasErrors()) {
            String newType = action.newDataType;
            if (!newType.toLowerCase().contains("varchar")) {
                errors.addWarning("modifyDataType will lose primary key/autoincrement/not null settings for mysql.  Use <sql> and re-specify all configuration if this is the case");
            }
        }

        return errors;
    }
}

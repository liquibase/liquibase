package liquibase.actionlogic.core.oracle;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.AddColumnsAction;
import liquibase.action.core.ColumnDefinition;
import liquibase.actionlogic.core.AddColumnsLogic;
import liquibase.database.Database;
import liquibase.database.core.oracle.OracleDatabase;
import liquibase.datatype.DataTypeFactory;

public class AddColumnsLogicOracle extends AddColumnsLogic {

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return OracleDatabase.class;
    }

    @Override
    protected String getDefaultValueClause(ColumnDefinition column, AddColumnsAction action, Scope scope) {
        Database database = scope.getDatabase();
        Object defaultValue = column.defaultValue;
        if (defaultValue != null) {
            if (defaultValue.toString().startsWith("GENERATED ALWAYS ")) {
                return DataTypeFactory.getInstance().fromObject(defaultValue, database).objectToSql(defaultValue, database);
            } else {
               return super.getDefaultValueClause(column, action, scope);
            }
        }
        return null;

    }
}

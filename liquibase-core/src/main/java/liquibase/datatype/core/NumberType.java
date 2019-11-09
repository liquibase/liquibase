package liquibase.datatype.core;

import liquibase.change.core.LoadDataChange;
import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;

import java.util.Arrays;

@DataTypeInfo(name="number", aliases = {"numeric", "java.sql.Types.NUMERIC"}, minParameters = 0, maxParameters = 2, priority = LiquibaseDataType.PRIORITY_DEFAULT)
public class NumberType extends LiquibaseDataType {

    private boolean autoIncrement;

    public boolean isAutoIncrement() {
        return autoIncrement;
    }

    public void setAutoIncrement(boolean autoIncrement) {
        this.autoIncrement = autoIncrement;
    }

    @Override
    public DatabaseDataType toDatabaseDataType(Database database) {
        if (database instanceof MSSQLDatabase) {
            Object[] parameters = getParameters();
            if (parameters.length == 0) {
                parameters = new Object[] { 18, 0 };
            } else if (parameters.length == 1) {
                parameters = new Object[] { parameters[0], 0 };
            } else if (parameters.length > 2) {
                parameters = Arrays.copyOfRange(parameters, 0, 2);
            }
            return new DatabaseDataType(database.escapeDataTypeName("numeric"), parameters);
        } else if ((database instanceof MySQLDatabase) || (database instanceof AbstractDb2Database) || (database instanceof
            HsqlDatabase) || (database instanceof DerbyDatabase) || (database instanceof FirebirdDatabase) ||
            (database instanceof InformixDatabase) || (database instanceof SybaseASADatabase) || (database instanceof
            SybaseDatabase)) {
            return new DatabaseDataType("numeric", getParameters());
        } else if (database instanceof OracleDatabase) {
            if ((getParameters().length > 1) && "0".equals(getParameters()[0]) && "-127".equals(getParameters()[1])) {
                return new DatabaseDataType("NUMBER");
            } else {
                return new DatabaseDataType("NUMBER", getParameters());
            }
        } else if (database instanceof PostgresDatabase) {
            if ((getParameters().length > 0) && (Integer.parseInt(getParameters()[0].toString()) > 1000)) {
                return new DatabaseDataType("numeric");
            }
            return new DatabaseDataType("numeric", getParameters());
        }
        return super.toDatabaseDataType(database);
    }

    @Override
    public LoadDataChange.LOAD_DATA_TYPE getLoadTypeName() {
        return LoadDataChange.LOAD_DATA_TYPE.NUMERIC;
    }
}

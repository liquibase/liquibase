package liquibase.datatype.core;

import liquibase.change.core.LoadDataChange;
import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;
import liquibase.statement.DatabaseFunction;

import java.util.Locale;

@DataTypeInfo(name="smallint", aliases = {"java.sql.Types.SMALLINT", "int2"}, minParameters = 0, maxParameters = 1, priority = LiquibaseDataType.PRIORITY_DEFAULT)
public class SmallIntType extends LiquibaseDataType {

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
            return new DatabaseDataType(database.escapeDataTypeName("smallint")); //always smallint regardless of parameters passed
        }
        if (database instanceof MySQLDatabase) {
            DatabaseDataType type = new DatabaseDataType("SMALLINT");
            type.addAdditionalInformation(getAdditionalInformation());
            return type;
        }
        if ((database instanceof AbstractDb2Database) || (database instanceof DerbyDatabase) || (database instanceof
            FirebirdDatabase) || (database instanceof InformixDatabase)) {
            return new DatabaseDataType("SMALLINT"); //always smallint regardless of parameters passed
        }

        if (database instanceof OracleDatabase) {
            return new DatabaseDataType("NUMBER", 5);
        }

	if (database instanceof PostgresDatabase) {
            if (isAutoIncrement()) {
                return new DatabaseDataType("SMALLSERIAL");
            } else {
		return new DatabaseDataType("SMALLINT");
	    }
        }
        return super.toDatabaseDataType(database);
    }

    @Override
    public String objectToSql(Object value, Database database) {
        if ((value == null) || "null".equals(value.toString().toLowerCase(Locale.US))) {
            return null;
        }
        if (value instanceof DatabaseFunction) {
            return value.toString();
        }

        if (value instanceof Boolean)
            return Boolean.TRUE.equals(value) ? "1" : "0";
        else {
            return formatNumber(value.toString());
        }
    }

    @Override
    public LoadDataChange.LOAD_DATA_TYPE getLoadTypeName() {
        return LoadDataChange.LOAD_DATA_TYPE.NUMERIC;
    }
}

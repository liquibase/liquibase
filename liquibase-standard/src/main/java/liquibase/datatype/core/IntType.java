package liquibase.datatype.core;

import liquibase.GlobalConfiguration;
import liquibase.change.core.LoadDataChange;
import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;
import liquibase.statement.DatabaseFunction;

import java.util.Locale;

/**
 * Represents a signed integer number using 32 bits of storage.
 */
@DataTypeInfo(name = "int", aliases = {"integer", "java.sql.Types.INTEGER", "java.lang.Integer", "serial", "int4", "serial4"}, minParameters = 0, maxParameters = 1, priority = LiquibaseDataType.PRIORITY_DEFAULT)
public class IntType extends LiquibaseDataType {

    private boolean autoIncrement;

    @Override
    public boolean isAutoIncrement() {
        return autoIncrement;
    }

    public void setAutoIncrement(boolean autoIncrement) {
        this.autoIncrement = autoIncrement;
    }


    @Override
    public DatabaseDataType toDatabaseDataType(Database database) {
        if ((database instanceof InformixDatabase) && isAutoIncrement()) {
            return new DatabaseDataType("SERIAL");
        }
        if ((database instanceof AbstractDb2Database) || (database instanceof DerbyDatabase) || database instanceof OracleDatabase) {
            return new DatabaseDataType("INTEGER");
        }

        if (database instanceof PostgresDatabase) {
            if (isAutoIncrement()) {
                if (((PostgresDatabase) database).useSerialDatatypes()) {
                    return new DatabaseDataType("SERIAL");
                } else {
                    if (GlobalConfiguration.CONVERT_DATA_TYPES.getCurrentValue() || this.getRawDefinition() == null) {
                        return new DatabaseDataType("INTEGER");
                    } else {
                        return new DatabaseDataType(this.getRawDefinition());
                    }
                }
            } else {
                return new DatabaseDataType("INTEGER");
            }
        }
        if (database instanceof MSSQLDatabase) {
            return new DatabaseDataType(database.escapeDataTypeName("int"));
        }
        if (database instanceof MySQLDatabase) {
            DatabaseDataType type = new DatabaseDataType("INT");
            type.addAdditionalInformation(getAdditionalInformation());
            return type;
        }
        if ((database instanceof HsqlDatabase) || (database instanceof FirebirdDatabase) || (database instanceof
                InformixDatabase) || (database instanceof SybaseDatabase)) {
            return new DatabaseDataType("INT");
        }
        if ((database instanceof SQLiteDatabase) || (database instanceof SybaseASADatabase)) {
            return new DatabaseDataType("INTEGER");
        }

        if (database instanceof H2Database && getParameters().length > 0) {
            int intParameter = Integer.parseInt(getParameters()[0].toString());
            if(intParameter >= 1 && intParameter <= 3) {
                return new DatabaseDataType("TINYINT");
            } else if (intParameter > 3 && intParameter <= 5) {
                return new DatabaseDataType("SMALLINT");
            } else if (intParameter > 5 && intParameter <= 10) {
                return new DatabaseDataType("INTEGER");
            } else if (intParameter > 10) {
                return new DatabaseDataType("BIGINT");
            }
        }
        return super.toDatabaseDataType(database);
    }

    @Override
    public void finishInitialization(String originalDefinition) {
        super.finishInitialization(originalDefinition);

        if (originalDefinition.toLowerCase(Locale.US).startsWith("serial")) {
            autoIncrement = true;
        }
    }

    @Override
    public String objectToSql(Object value, Database database) {
        if ((value == null) || "null".equals(value.toString().toLowerCase(Locale.US))) {
            return null;
        }
        if (value instanceof DatabaseFunction) {
            return value.toString();
        }

        return formatNumber(value.toString());
    }

    @Override
    public LoadDataChange.LOAD_DATA_TYPE getLoadTypeName() {
        return LoadDataChange.LOAD_DATA_TYPE.NUMERIC;
    }

}

package liquibase.datatype.core;

import liquibase.change.core.LoadDataChange;
import liquibase.database.Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;
import liquibase.statement.DatabaseFunction;

import java.util.Arrays;

/**
 * Container for a data type that is not covered by any implementation in {@link liquibase.datatype.core}. Most often,
 * this class is used when a DBMS-specific data type is given of which Liquibase does not know anything about yet.
 */
public class UnknownType extends LiquibaseDataType {

    public UnknownType() {
        super("UNKNOWN", 0, 2);
    }

    public UnknownType(String name) {
        super(name, 0, 2);
    }

    public UnknownType(String name, int minParameters, int maxParameters) {
        super(name, minParameters, maxParameters);
    }

    private boolean autoIncrement;

    public boolean isAutoIncrement() {
        return autoIncrement;
    }

    public void setAutoIncrement(boolean autoIncrement) {
        this.autoIncrement = autoIncrement;
    }

    @Override
    public DatabaseDataType toDatabaseDataType(Database database) {
        int dataTypeMaxParameters;
        if ("enum".equalsIgnoreCase(getName()) || "set".equalsIgnoreCase(getName())) {
            dataTypeMaxParameters = Integer.MAX_VALUE;
        } else {
            dataTypeMaxParameters = database.getDataTypeMaxParameters(getName());
        }
        Object[] parameters = getParameters();

        if (database instanceof OracleDatabase) {
            if ("LONG".equalsIgnoreCase(getName())
                    || "BFILE".equalsIgnoreCase(getName())
                    || "ROWID".equalsIgnoreCase(getName())
                    || "ANYDATA".equalsIgnoreCase(getName())
                    || "SDO_GEOMETRY".equalsIgnoreCase(getName())
                    ) {
                parameters = new Object[0];
            } else if ("RAW".equalsIgnoreCase(getName())) {
                return new DatabaseDataType(getName(), parameters);
            } else if (getName().toUpperCase().startsWith("INTERVAL ")) {
                return new DatabaseDataType(getName().replaceAll("\\(\\d+\\)", ""));
            } else {
                // probably a user defined type. Can't call getUserDefinedTypes() to know for sure, since that returns
                // all types including system types.
                return new DatabaseDataType(getName().toUpperCase());
            }
        }

        if (dataTypeMaxParameters < parameters.length) {
            parameters = Arrays.copyOfRange(parameters, 0, dataTypeMaxParameters);
        }
        DatabaseDataType type;
        if (database instanceof MSSQLDatabase) {
            if ( (parameters.length >= 1) &&
                (this.getRawDefinition().matches("(?i)\\[?datetimeoffset\\]?.*")) &&
                (Integer.parseInt(parameters[0].toString()) ==
                    (database.getDefaultScaleForNativeDataType("datetimeoffset"))) ) {
                parameters = new Object[0];
            }
            type = new DatabaseDataType(database.escapeDataTypeName(getName()), parameters);
        } else {
            type = new DatabaseDataType(getName().toUpperCase(), parameters);
        }
        type.addAdditionalInformation(getAdditionalInformation());

        return type;
    }

    @Override
    public String objectToSql(Object value, Database database) {
        if (value instanceof DatabaseFunction) {
            return super.objectToSql(value, database);
        } else {
            return "'"+super.objectToSql(value, database)+"'";
        }
    }

    @Override
    public LoadDataChange.LOAD_DATA_TYPE getLoadTypeName() {
        return LoadDataChange.LOAD_DATA_TYPE.STRING;
    }
}

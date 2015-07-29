package liquibase.datatype.core;

import liquibase.database.Database;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;
import liquibase.statement.DatabaseFunction;

import java.util.Arrays;
import java.util.Collections;

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

    @Override
    public DatabaseDataType toDatabaseDataType(Database database) {
        int dataTypeMaxParameters;
        if (getName().equalsIgnoreCase("enum") || getName().equalsIgnoreCase("set")) {
            dataTypeMaxParameters = Integer.MAX_VALUE;
        } else {
            dataTypeMaxParameters = database.getDataTypeMaxParameters(getName());
        }
        Object[] parameters = getParameters();

//        if (database instanceof OracleDatabase) {
//            if (getName().equalsIgnoreCase("LONG")
//                    || getName().equalsIgnoreCase("BFILE")
//                    || getName().equalsIgnoreCase("ROWID")
//                    || getName().equalsIgnoreCase("ANYDATA")
//                    || getName().equalsIgnoreCase("SDO_GEOMETRY")
//                    ) {
//                parameters = new Object[0];
//            } else if (getName().toUpperCase().startsWith("INTERVAL ")) {
//                return new DatabaseDataType(getName().replaceAll("\\(\\d+\\)", ""));
//            } else if (((OracleDatabase) database).getUserDefinedTypes().contains(getName().toUpperCase())) {
//                return new DatabaseDataType(getName().toUpperCase()); //user defined tye
//            }
//        }

        if (dataTypeMaxParameters < parameters.length) {
            parameters = Arrays.copyOfRange(parameters, 0, dataTypeMaxParameters);
        }
        DatabaseDataType type;
//        if (database instanceof  MSSQLDatabase) {
//            type = new DatabaseDataType(database.escapeDataTypeName(getName()), parameters);
//        } else {
            type = new DatabaseDataType(getName().toUpperCase(), parameters);
//        }
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
}

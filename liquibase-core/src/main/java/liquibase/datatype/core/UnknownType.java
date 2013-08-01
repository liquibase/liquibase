package liquibase.datatype.core;

import liquibase.database.Database;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.core.MSSQLDatabase;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;

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
        int dataTypeMaxParameters = database.getDataTypeMaxParameters(getName());
        Object[] parameters = getParameters();
		
		/*
		* Address CORE-1356 & CORE-1358 by handling types returned with unnecesarry size specifiers
		* as Unknown Types
		*/
        if (database instanceof MySQLDatabase && (
                getName().equalsIgnoreCase("TINYBLOB")
                        || getName().equalsIgnoreCase("MEDIUMBLOB")
                        || getName().equalsIgnoreCase("TINYTEXT")
                        || getName().equalsIgnoreCase("MEDIUMTEXT")
        )|| (database instanceof MSSQLDatabase && getName().equalsIgnoreCase("REAL"))) {
            parameters = new Object[0];
        }

        if (dataTypeMaxParameters < parameters.length) {
            parameters = Arrays.copyOfRange(parameters, 0, dataTypeMaxParameters);
        }
        DatabaseDataType type = new DatabaseDataType(getName().toUpperCase(), parameters);
        type.addAdditionalInformation(getAdditionalInformation());

        return type;
    }
}

package liquibase.datatype.core;

import liquibase.change.core.LoadDataChange;
import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Locale;

@DataTypeInfo(name="nvarchar", aliases = {"java.sql.Types.NVARCHAR", "nvarchar2", "national"}, minParameters = 0, maxParameters = 1, priority = LiquibaseDataType.PRIORITY_DEFAULT)
public class NVarcharType extends CharType {

    @Override
    public DatabaseDataType toDatabaseDataType(Database database) {
        if ((getRawDefinition() != null) && getRawDefinition().toLowerCase(Locale.US).contains("national character varying")) {
            setAdditionalInformation(null); //just go to nvarchar
        }
        if ((database instanceof HsqlDatabase) || (database instanceof PostgresDatabase) || (database instanceof
            DerbyDatabase)) {

            return new DatabaseDataType("VARCHAR", getParameters());
        }
        if (database instanceof OracleDatabase) {
            return new DatabaseDataType("NVARCHAR2", getParameters());
        }
        if (database instanceof MSSQLDatabase) {
            Object[] parameters = getParameters();
            if (parameters.length > 0) {
                String param1 = parameters[0].toString();
                if (!param1.matches("\\d+") || (new BigInteger(param1).compareTo(BigInteger.valueOf(4000L)) > 0)) {

                    DatabaseDataType type = new DatabaseDataType(database.escapeDataTypeName("nvarchar"), "MAX");
                    type.addAdditionalInformation(getAdditionalInformation());
                    return type;
                }
            }
            if (parameters.length == 0) {
                parameters = new Object[] { 1 };
            } else if (parameters.length > 1) {
                parameters = Arrays.copyOfRange(parameters, 0, 1);
            }
            DatabaseDataType type =  new DatabaseDataType(database.escapeDataTypeName("nvarchar"), parameters);
            type.addAdditionalInformation(getAdditionalInformation());
            return type;
        }
        return super.toDatabaseDataType(database);
    }

    @Override
    public LoadDataChange.LOAD_DATA_TYPE getLoadTypeName() {
        return LoadDataChange.LOAD_DATA_TYPE.STRING;
    }

}

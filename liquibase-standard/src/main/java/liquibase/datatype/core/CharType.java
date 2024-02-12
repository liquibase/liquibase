package liquibase.datatype.core;

import liquibase.Scope;
import liquibase.change.core.LoadDataChange;
import liquibase.database.Database;
import liquibase.database.core.H2Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;
import liquibase.integration.commandline.LiquibaseCommandLineConfiguration;
import liquibase.statement.DatabaseFunction;
import liquibase.util.StringUtil;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@DataTypeInfo(name="char", aliases = {"java.sql.Types.CHAR", "bpchar", "character"}, minParameters = 0, maxParameters = 1, priority = LiquibaseDataType.PRIORITY_DEFAULT)
public class CharType extends LiquibaseDataType {
    @Override
    public DatabaseDataType toDatabaseDataType(Database database) {
        if (database instanceof MSSQLDatabase) {
            Object[] parameters = getParameters();
            if (parameters.length > 0) {
                // MSSQL only supports (n) syntax but not (n CHAR) syntax, so we need to remove CHAR.
                final String param1 = parameters[0].toString().replaceFirst("(?<=\\d+)\\s*(?i)CHAR$", "");
                parameters[0] =  param1;
                if (!param1.matches("\\d+") || (new BigInteger(param1).compareTo(BigInteger.valueOf(8000)) > 0)) {

                    DatabaseDataType type = new DatabaseDataType(database.escapeDataTypeName("char"), 8000);
                    type.addAdditionalInformation(getAdditionalInformation());
                    return type;
                }
            }
            if (parameters.length == 0) {
                parameters = new Object[] { 1 };
            } else if (parameters.length > 1) {
                parameters = Arrays.copyOfRange(parameters, 0, 1);
            }
            DatabaseDataType type = new DatabaseDataType(database.escapeDataTypeName("char"), parameters);
            type.addAdditionalInformation(getAdditionalInformation());
            return type;
        } else if (database instanceof PostgresDatabase){
            final Object[] parameters = getParameters();
            if ((parameters != null) && (parameters.length == 1)) {
                // PostgreSQL only supports (n) syntax but not (n CHAR) syntax, so we need to remove CHAR.
                final String parameter = parameters[0].toString().replaceFirst("(?<=\\d+)\\s*(?i)CHAR$", "");
                // PostgreSQL uses max. length implicitly if no length is provided to CHARACTER VARYING, so we can spare it.
                if ("2147483647".equals(parameter)) {
                    DatabaseDataType type = new DatabaseDataType("CHARACTER");
                    type.addAdditionalInformation("VARYING");
                    return type;
                }
                parameters[0] = parameter;
                DatabaseDataType type = new DatabaseDataType(this.getName().toUpperCase(Locale.US), parameters);
                type.addAdditionalInformation(this.getAdditionalInformation());
                return type;
            }
        } else if (database instanceof H2Database) {
            if (getRawDefinition().toLowerCase(Locale.US).contains("large object")) {
                return new DatabaseDataType("CHARACTER LARGE OBJECT");
            }
        }

        return super.toDatabaseDataType(database);
    }

    @Override
    public String objectToSql(Object value, Database database) {
        if ((value == null) || "null".equals(value.toString().toLowerCase(Locale.US))) {
            return null;
        }
        String stringValue = value.toString();

        if (value instanceof DatabaseFunction) {
            return stringValue;
        }

        String val = String.valueOf(value);
        if ((database instanceof MSSQLDatabase) && !StringUtil.isAscii(val)) {
            return "N'"+database.escapeStringForDatabase(val)+"'";
        }

        /*
          It is a somewhat safe assumption that if the database is Oracle and the length of the string exceeds 4000
          characters then the column must be a clob type column, because Oracle doesn't support varchars longer than
          2000 characters. It would be better to read the column config directly, but that info isn't available at this
          point in the code.
         */
        if (database instanceof OracleDatabase &&
                LiquibaseCommandLineConfiguration.WORKAROUND_ORACLE_CLOB_CHARACTER_LIMIT.getCurrentValue() &&
                stringValue.length() > 4000) {
            Scope.getCurrentScope().getLog(getClass()).fine("A string longer than 4000 characters has been detected on an insert statement, " +
                    "and the database is Oracle. Oracle forbids insert statements with strings longer than 4000 characters, " +
                    "so Liquibase is going to workaround this limitation. If an error occurs, this can be disabled by setting "
                    + LiquibaseCommandLineConfiguration.WORKAROUND_ORACLE_CLOB_CHARACTER_LIMIT.getKey() + " to false.");
            List<String> chunks = StringUtil.splitToChunks(stringValue, 4000);
            return "to_clob( '" + StringUtil.join(chunks, "' ) || to_clob( '", obj -> database.escapeStringForDatabase(obj.toString())) + "' )";
        }

        return "'"+database.escapeStringForDatabase(val)+"'";
    }

    /**
     * Return the size of this data type definition. If unknown or unspecified, return -1
     */
    protected int getSize() {
        if (getParameters().length == 0) {
            return -1;
        }

        if (getParameters()[0] instanceof String) {
            return Integer.parseInt((String) getParameters()[0]);
        }

        if (getParameters()[0] instanceof Number) {
            return ((Number) getParameters()[0]).intValue();
        }

        return -1;
    }

    @Override
    public LoadDataChange.LOAD_DATA_TYPE getLoadTypeName() {
        return LoadDataChange.LOAD_DATA_TYPE.STRING;
    }

}

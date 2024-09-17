package liquibase.datatype.core;

import liquibase.GlobalConfiguration;
import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;
import liquibase.util.StringUtil;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Locale;

@DataTypeInfo(name="varchar", aliases = {"java.lang.String", "varchar2", "character varying", "longvarchar", "text", "longtext", "java.sql.Types.LONGVARCHAR", "long varchar", "tinytext", "mediumtext", "java.sql.Types.VARCHAR"}, minParameters = 0, maxParameters = 1, priority = LiquibaseDataType.PRIORITY_DEFAULT)
public class VarcharType extends CharType {

    @Override
    public DatabaseDataType toDatabaseDataType(Database database) {
        String originalDefinition = StringUtil.trimToEmpty(getRawDefinition());
        if ((database instanceof OracleDatabase) || ((database instanceof HsqlDatabase) && ((HsqlDatabase) database)
            .isUsingOracleSyntax())) {
            return new DatabaseDataType("VARCHAR2", getParameters());
        }

        if ((database instanceof InformixDatabase) && (getSize() > 255)) {
            return new DatabaseDataType("LVARCHAR", getParameters());
        }

        if (database instanceof MSSQLDatabase) {
            if (originalDefinition != null && originalDefinition.toLowerCase(Locale.US).startsWith("text") ||
                    originalDefinition.toLowerCase(Locale.US).startsWith("[text]")){
                if (! Boolean.TRUE.equals(GlobalConfiguration.CONVERT_DATA_TYPES.getCurrentValue())) {
                    return new DatabaseDataType(database.escapeDataTypeName(originalDefinition));
                }
                DatabaseDataType type = new DatabaseDataType(database.escapeDataTypeName("varchar"));

                // If there is additional specification after text (e.g.  COLLATE), import that.
                String originalExtraInfo = originalDefinition.replaceFirst("^(?i)\\[?ntext\\]?\\s*", "");
                originalExtraInfo = originalExtraInfo.replaceFirst("^(?i)\\[?text\\]?\\s*", "");
                type.addAdditionalInformation("(max)");
                if(!StringUtil.isEmpty(originalExtraInfo)) {
                    //if we still have something like (25555) remove it
                    //since we already set it to max, otherwise add collate or other info
                    if(originalExtraInfo.lastIndexOf(")") < (originalExtraInfo.length() - 1)) {
                        type.addAdditionalInformation(originalExtraInfo.substring(originalExtraInfo.lastIndexOf(")") + 1));
                    }
                }
                return type;
            } else {
                Object[] parameters = getParameters();
                if (parameters.length > 0) {
                    // MSSQL only supports (n) syntax but not (n CHAR) syntax, so we need to remove CHAR.
                    final String param1 = parameters[0].toString().replaceFirst("(?<=\\d+)\\s*(?i)CHAR$", "");
                    parameters[0] =  param1;
                    if (!param1.matches("\\d+") || (new BigInteger(param1).compareTo(BigInteger.valueOf(8000L)) > 0)) {

                        DatabaseDataType type = new DatabaseDataType(database.escapeDataTypeName("varchar"), "MAX");
                        type.addAdditionalInformation(getAdditionalInformation());
                        return type;
                    }
                }
                if (parameters.length == 0) {
                    parameters = new Object[] { 1 };
                } else if (parameters.length > 1) {
                    parameters = Arrays.copyOfRange(parameters, 0, 1);
                }
                DatabaseDataType type = new DatabaseDataType(database.escapeDataTypeName("varchar"), parameters);
                type.addAdditionalInformation(getAdditionalInformation());
                return type;
            }

        } else if (database instanceof PostgresDatabase) {
            final Object[] parameters = getParameters();
            if ((parameters != null) && (parameters.length == 1)) {
                // PostgreSQL only supports (n) syntax but not (n CHAR) syntax, so we need to remove CHAR.
                final String parameter = parameters[0].toString().replaceFirst("(?<=\\d+)\\s*(?i)CHAR$", "");
                // PostgreSQL uses max. length implicitly if no length is provided, so we can spare it.
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
        } else if (database instanceof MySQLDatabase){
            if (originalDefinition.toLowerCase(Locale.US).startsWith("text")) {
                return new DatabaseDataType("TEXT");
            } else if (originalDefinition.toLowerCase(Locale.US).startsWith("tinytext")) {
                return new DatabaseDataType("TINYTEXT");
            } else if (originalDefinition.toLowerCase(Locale.US).startsWith("mediumtext")) {
                return new DatabaseDataType("MEDIUMTEXT");}
        } else if ((database instanceof SQLiteDatabase) || (database
                instanceof SybaseDatabase)) {
            if (originalDefinition.toLowerCase(Locale.US).startsWith("character varying") || originalDefinition.toLowerCase(Locale.US).startsWith("varchar")) {
                Object[] parameters = getParameters();
                if (parameters.length > 0) {
                    if (!parameters[0].toString().matches("\\d+") || (new BigInteger(parameters[0].toString()).compareTo(BigInteger.valueOf(8000L)) > 0)) {
                        DatabaseDataType type = new DatabaseDataType(database.escapeDataTypeName("varchar"), "MAX");
                        type.addAdditionalInformation(getAdditionalInformation());
                        return type;
                    }
                }
                if (parameters.length > 1) {
                    parameters = Arrays.copyOfRange(parameters, 0, 1);
                }
                return new DatabaseDataType("VARCHAR",parameters);
            }else if ((database instanceof H2Database) || (database instanceof HsqlDatabase)) {
                if (originalDefinition.toLowerCase(Locale.US).startsWith("longvarchar") || originalDefinition.startsWith("java.sql.Types.LONGVARCHAR")) {
                    return new DatabaseDataType("LONGVARCHAR");
                }
            } else if (database instanceof InformixDatabase) {
                if (originalDefinition.toLowerCase(Locale.US).startsWith("text")) {
                    return new DatabaseDataType("TEXT");
                }
            }
            return new DatabaseDataType("TEXT");
        }

        return super.toDatabaseDataType(database);
    }


}

package liquibase.datatype.core;

import liquibase.GlobalConfiguration;
import liquibase.change.core.LoadDataChange;
import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;
import liquibase.util.StringUtil;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Locale;

@DataTypeInfo(name="nvarchar", aliases = {"java.sql.Types.NVARCHAR", "nvarchar2", "national", "longnvarchar", "ntext", "java.sql.Types.LONGNVARCHAR", "long nvarchar"}, minParameters = 0, maxParameters = 1, priority = LiquibaseDataType.PRIORITY_DEFAULT)
public class NVarcharType extends CharType {

    @Override
    public DatabaseDataType toDatabaseDataType(Database database) {
        String originalDefinition = StringUtil.trimToEmpty(getRawDefinition());
        if ((originalDefinition != null) && originalDefinition.toLowerCase(Locale.US).contains("national character varying")) {
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

            if (originalDefinition.toLowerCase(Locale.US).startsWith("ntext") ||
                    originalDefinition.toLowerCase(Locale.US).startsWith("[ntext]")){
                if (! Boolean.TRUE.equals(GlobalConfiguration.CONVERT_DATA_TYPES.getCurrentValue())) {
                    return new DatabaseDataType(database.escapeDataTypeName(originalDefinition));
                }
                DatabaseDataType type = new DatabaseDataType(database.escapeDataTypeName("nvarchar"));
                // If there is additional specification after ntext (e.g.  COLLATE), import that.
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
        } else if ((database instanceof PostgresDatabase) || (database instanceof SQLiteDatabase) || (database
                instanceof SybaseDatabase)) {
            return new DatabaseDataType("TEXT");
        }
        return super.toDatabaseDataType(database);
    }

    @Override
    public LoadDataChange.LOAD_DATA_TYPE getLoadTypeName() {
        return LoadDataChange.LOAD_DATA_TYPE.STRING;
    }

}

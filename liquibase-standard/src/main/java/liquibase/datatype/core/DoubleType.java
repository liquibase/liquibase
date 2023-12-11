package liquibase.datatype.core;

import liquibase.change.core.LoadDataChange;
import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;
import liquibase.util.StringUtil;

import java.util.Locale;

@DataTypeInfo(name="double", aliases = {"java.sql.Types.DOUBLE", "java.lang.Double"}, minParameters = 0, maxParameters = 2, priority = LiquibaseDataType.PRIORITY_DEFAULT)
public class DoubleType  extends LiquibaseDataType {
    @Override
    public DatabaseDataType toDatabaseDataType(Database database) {
        if (database instanceof MSSQLDatabase) {
            return new DatabaseDataType(database.escapeDataTypeName("float"), 53);
        }
        if (database instanceof MySQLDatabase) {
            DatabaseDataType datatype;
            String additionalInfo = StringUtil.trimToEmpty(getAdditionalInformation()).toUpperCase(Locale.US);
            String name = "DOUBLE";
            if (additionalInfo.contains("PRECISION")) {
                name += " PRECISION";
                additionalInfo = additionalInfo.replace("PRECISION", "");
            }

            if ((getParameters() != null) && (getParameters().length > 1)) {
                datatype = new DatabaseDataType(name, getParameters());
            } else {
                datatype = new DatabaseDataType(name);
            }

            additionalInfo = additionalInfo.replaceAll("\\s+", " ");
            datatype.addAdditionalInformation(StringUtil.trimToNull(additionalInfo));
            return datatype;
        }
        if ((database instanceof AbstractDb2Database) || (database instanceof DerbyDatabase) || (database instanceof HsqlDatabase) || (database instanceof SybaseASADatabase)) {
            return new DatabaseDataType("DOUBLE");
        }
        if ((database instanceof H2Database) || (database instanceof OracleDatabase) || (database instanceof PostgresDatabase)
              || (database instanceof InformixDatabase) || (database instanceof FirebirdDatabase)) {
            return new DatabaseDataType("DOUBLE PRECISION");
        }
        return super.toDatabaseDataType(database);
    }

    @Override
    public LoadDataChange.LOAD_DATA_TYPE getLoadTypeName() {
        return LoadDataChange.LOAD_DATA_TYPE.NUMERIC;
    }
}

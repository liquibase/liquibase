package liquibase.datatype.core;

import liquibase.database.Database;
import liquibase.database.core.H2Database;
import liquibase.database.core.HsqlDatabase;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;

@DataTypeInfo(name="longvarchar", aliases = {"java.sql.Types.LONGVARCHAR"}, minParameters = 0, maxParameters = 0, priority = LiquibaseDataType.PRIORITY_DEFAULT)

public class LongVarcharType extends ClobType {

    @Override
    public DatabaseDataType toDatabaseDataType(Database database) {
        if (database instanceof H2Database || database instanceof HsqlDatabase) {
            return new DatabaseDataType("LONGVARCHAR");
        } else {
            return super.toDatabaseDataType(database);
        }
    }
}

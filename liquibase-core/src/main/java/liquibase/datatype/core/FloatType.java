package liquibase.datatype.core;

import liquibase.database.Database;
import liquibase.database.core.DB2Database;
import liquibase.database.core.DerbyDatabase;
import liquibase.database.core.FirebirdDatabase;
import liquibase.database.core.InformixDatabase;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;

@DataTypeInfo(name="float", aliases = {"java.sql.Types.FLOAT","java.lang.Float"}, minParameters = 0, maxParameters = 2, priority = LiquibaseDataType.PRIORITY_DEFAULT)
public class FloatType  extends LiquibaseDataType {

    @Override
    public DatabaseDataType toDatabaseDataType(Database database) {
        if (database instanceof FirebirdDatabase || database instanceof InformixDatabase) {
            return new DatabaseDataType("FLOAT");
        }
        return super.toDatabaseDataType(database);
    }

    //sqlite
    //        } else if (columnTypeString.equals("REAL") ||
//                columnTypeString.toLowerCase(Locale.ENGLISH).contains("float")) {
    //            type = new FloatType("REAL");



    //postgres
    //        } else if (type.toDatabaseDataType().toLowerCase().startsWith("float8")) {
//            type.setDataTypeName("FLOAT8");
//        } else if (type.toDatabaseDataType().toLowerCase().startsWith("float4")) {
//            type.setDataTypeName("FLOAT4");

}

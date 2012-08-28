package liquibase.datatype.core;

import liquibase.database.Database;
import liquibase.database.core.DB2Database;
import liquibase.database.core.InformixDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;

@DataTypeInfo(name="int",aliases = {"integer", "java.sql.Types.INTEGER", "java.lang.Integer"}, minParameters = 0, maxParameters = 1, priority = LiquibaseDataType.PRIORITY_DEFAULT)
public class IntType  extends LiquibaseDataType {

    private boolean autoIncrement;

    public boolean isAutoIncrement() {
        return autoIncrement;
    }

    public void setAutoIncrement(boolean autoIncrement) {
        this.autoIncrement = autoIncrement;
    }


    @Override
    public DatabaseDataType toDatabaseDataType(Database database) {
        if (database instanceof InformixDatabase && isAutoIncrement()) {
            return new DatabaseDataType("SERIAL");
        }
        if (database instanceof OracleDatabase) {
            return new DatabaseDataType("INTEGER", getParameters());
        }
        if (database instanceof DB2Database) {
            return new DatabaseDataType("INTEGER");
        }
        return super.toDatabaseDataType(database);
    }

    //sqllite
    //        if (columnTypeString.equals("INTEGER") ||
//                columnTypeString.toLowerCase(Locale.ENGLISH).contains("int") ||
//                columnTypeString.toLowerCase(Locale.ENGLISH).contains("bit")) {
//            type = new IntType("INTEGER");


    //postgres
    //        if (autoIncrement != null && autoIncrement) {
//            if ("integer".equals(type.toDatabaseDataType().toLowerCase())) {
//                type.setDataTypeName("serial");
//            } else if ("bigint".equals(type.toDatabaseDataType().toLowerCase()) || "bigserial".equals(type.toDatabaseDataType().toLowerCase())) {
//                type.setDataTypeName("bigserial");
//            } else {
//                // Unknown integer type, default to "serial"
//                type.setDataTypeName("serial");
//            }
//        }

}

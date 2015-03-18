package liquibase.datatype.core;

import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;
import liquibase.statement.DatabaseFunction;

@DataTypeInfo(name = "int", aliases = { "integer", "java.sql.Types.INTEGER", "java.lang.Integer", "serial", "int4", "serial4" }, minParameters = 0, maxParameters = 1, priority = LiquibaseDataType.PRIORITY_DEFAULT)
public class IntType extends LiquibaseDataType {

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
            return new DatabaseDataType("NUMBER", 10);
        }
        if (database instanceof DB2Database || database instanceof DerbyDatabase) {
            return new DatabaseDataType("INTEGER");
        }
        if (database instanceof PostgresDatabase) {
            if (autoIncrement) {
                return new DatabaseDataType("SERIAL");
            }
        }
        if (database instanceof MSSQLDatabase || database instanceof HsqlDatabase || database instanceof FirebirdDatabase || database instanceof InformixDatabase  || database instanceof MySQLDatabase) {
            return new DatabaseDataType("INT");
        }
        if (database instanceof SQLiteDatabase) {
        	return new DatabaseDataType("INTEGER");
        }
        return super.toDatabaseDataType(database);

        //sqllite
        //        if (columnTypeString.equals("INTEGER") ||
//                columnTypeString.toLowerCase(Locale.ENGLISH).contains("int") ||
//                columnTypeString.toLowerCase(Locale.ENGLISH).contains("bit")) {
//            type = new IntType("INTEGER");
    }

    @Override
    public void finishInitialization(String originalDefinition) {
        super.finishInitialization(originalDefinition);

        if (originalDefinition.toLowerCase().startsWith("serial")) {
            autoIncrement = true;
        }
    }

    @Override
    public String objectToSql(Object value, Database database) {
        if (value == null || value.toString().equalsIgnoreCase("null")) {
            return null;
        }
        if (value instanceof DatabaseFunction) {
            return value.toString();
        }

        return formatNumber(value.toString());
    }


}

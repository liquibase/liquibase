package liquibase.datatype.core;

import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;

@DataTypeInfo(name="number", aliases = {"numeric", "java.sql.Types.NUMERIC"}, minParameters = 0, maxParameters = 0, priority = LiquibaseDataType.PRIORITY_DEFAULT)
public class NumberType extends LiquibaseDataType {

    private boolean autoIncrement;

    public boolean isAutoIncrement() {
        return autoIncrement;
    }

    public void setAutoIncrement(boolean autoIncrement) {
        this.autoIncrement = autoIncrement;
    }

    @Override
    public DatabaseDataType toDatabaseDataType(Database database) {
        if (database instanceof MySQLDatabase
                || database instanceof DB2Database
                || database instanceof MSSQLDatabase
                || database instanceof HsqlDatabase
                || database instanceof DerbyDatabase
                || database instanceof PostgresDatabase
                || database instanceof FirebirdDatabase
                || database instanceof SybaseASADatabase
                || database instanceof SybaseDatabase) {
            return new DatabaseDataType("numeric", getParameters());
        }

        if (database instanceof OracleDatabase) {
            if (getParameters().length > 0 && getParameters()[0].equals("0") && getParameters()[1].equals("-127")) {
                return new DatabaseDataType("NUMBER");
            } else {
                return new DatabaseDataType("NUMBER", getParameters());
            }
        }
        return super.toDatabaseDataType(database);
    }
}

package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;
import lombok.Getter;

import java.math.BigInteger;

@Getter
public class AddAutoIncrementStatement extends AbstractSqlStatement {

    private final String columnName;
    private final String columnDataType;
    private final BigInteger startWith;
    private final BigInteger incrementBy;
    private final Boolean defaultOnNull;
    private final String generationType;
    private DatabaseTableIdentifier databaseTableIdentifier = new DatabaseTableIdentifier(null, null, null);

    public AddAutoIncrementStatement(
            String catalogName,
            String schemaName,
            String tableName,
            String columnName,
            String columnDataType,
            BigInteger startWith,
            BigInteger incrementBy,
            Boolean defaultOnNull,
            String generationType) {
        this.databaseTableIdentifier.setCatalogName(catalogName);
        this.databaseTableIdentifier.setSchemaName(schemaName);
        this.databaseTableIdentifier.setTableName(tableName);
        this.columnName = columnName;
        this.columnDataType = columnDataType;
        this.startWith = startWith;
        this.incrementBy = incrementBy;
        this.defaultOnNull = defaultOnNull;
        this.generationType = generationType;
    }
    public String getCatalogName() {
        return databaseTableIdentifier.getCatalogName();
    }
    public String getSchemaName() {
        return databaseTableIdentifier.getSchemaName();
    }
    public String getTableName() {
        return databaseTableIdentifier.getTableName();
    }

}

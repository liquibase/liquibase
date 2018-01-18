package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

import java.math.BigInteger;

public class AddAutoIncrementStatement extends AbstractSqlStatement {

    private String catalogName;
    private String schemaName;
    private String tableName;
    private String columnName;
    private String columnDataType;
    private BigInteger startWith;
    private BigInteger incrementBy;
    
    public AddAutoIncrementStatement(
            String catalogName,
            String schemaName,
            String tableName,
            String columnName,
            String columnDataType,
            BigInteger startWith,
            BigInteger incrementBy) {
        this.catalogName = catalogName;
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.columnName = columnName;
        this.columnDataType = columnDataType;
        this.startWith = startWith;
        this.incrementBy = incrementBy;
    }

    public String getCatalogName() {
        return catalogName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getTableName() {
        return tableName;
    }

    public String getColumnName() {
        return columnName;
    }

    public String getColumnDataType() {
        return columnDataType;
    }
    
    public BigInteger getStartWith() {
        return startWith;
    }
    
    public BigInteger getIncrementBy() {
        return incrementBy;
    }
}

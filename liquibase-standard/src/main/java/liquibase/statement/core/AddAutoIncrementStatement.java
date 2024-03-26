package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;
import lombok.Getter;

import java.math.BigInteger;

@Getter
public class AddAutoIncrementStatement extends AbstractSqlStatement {

    private final String catalogName;
    private final String schemaName;
    private final String tableName;
    private final String columnName;
    private final String columnDataType;
    private final BigInteger startWith;
    private final BigInteger incrementBy;
    private final Boolean defaultOnNull;
    private final String generationType;

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
        this.catalogName = catalogName;
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.columnName = columnName;
        this.columnDataType = columnDataType;
        this.startWith = startWith;
        this.incrementBy = incrementBy;
        this.defaultOnNull = defaultOnNull;
        this.generationType = generationType;
    }

}

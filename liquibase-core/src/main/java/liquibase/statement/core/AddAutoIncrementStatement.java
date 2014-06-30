package liquibase.statement.core;

import liquibase.statement.AbstractStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;

import java.math.BigInteger;

/**
 * Marks an existing column as auto-increment.
 */
public class AddAutoIncrementStatement extends AbstractColumnStatement {

    private final String COLUMN_DATA_TYPE = "columnDataType";
    private final String STARTS_WITH = "startWith";
    private final String INCREMENT_BY = "incrementBy";

    public AddAutoIncrementStatement() {
    }

    public AddAutoIncrementStatement(
            String catalogName,
            String schemaName,
            String tableName,
            String columnName,
            String columnDataType,
            BigInteger startWith,
            BigInteger incrementBy) {
        super(catalogName, schemaName, tableName, columnName);
        setColumnDataType(columnDataType);
        setStartWith(startWith);
        setIncrementBy(incrementBy);
    }

    public String getColumnDataType() {
        return getAttribute(COLUMN_DATA_TYPE, String.class);
    }

    public AddAutoIncrementStatement setColumnDataType(String columnDataType) {
        return (AddAutoIncrementStatement) setAttribute(COLUMN_DATA_TYPE, columnDataType);
    }


    public BigInteger getStartWith() {
        return getAttribute(STARTS_WITH, BigInteger.class);
    }

    public AddAutoIncrementStatement setStartWith(BigInteger startWith) {
        return (AddAutoIncrementStatement) setAttribute(STARTS_WITH, startWith);
    }


    public BigInteger getIncrementBy() {
        return getAttribute(INCREMENT_BY, BigInteger.class);
    }

    public AddAutoIncrementStatement setIncrementBy(BigInteger incrementBy) {
        return (AddAutoIncrementStatement) setAttribute(INCREMENT_BY, incrementBy);
    }


    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        return new DatabaseObject[]{
                new Column()
                        .setRelation(new Table().setName(getTableName()).setSchema(new Schema(getCatalogName(), getSchemaName())))
                        .setName(getColumnName())
        };
    }
}

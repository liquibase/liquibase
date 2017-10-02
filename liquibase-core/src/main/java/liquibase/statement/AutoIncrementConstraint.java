package liquibase.statement;

import java.math.BigInteger;

public class AutoIncrementConstraint implements ColumnConstraint {
    private String columnName;
    private BigInteger startWith;
    private BigInteger incrementBy;
    
    public AutoIncrementConstraint() {
    }

    public AutoIncrementConstraint(String columnName) {
        setColumnName(columnName);
    }

    public AutoIncrementConstraint(
            String columnName, BigInteger startWith, BigInteger incrementBy) {
        this(columnName);
        setStartWith(startWith);
        setIncrementBy(incrementBy);
    }

    public String getColumnName() {
        return columnName;
    }

    public AutoIncrementConstraint setColumnName(String columnName) {
        this.columnName = columnName;
        return this;
    }

    public BigInteger getStartWith() {
        return startWith;
    }
    
    public AutoIncrementConstraint setStartWith(BigInteger startWith) {
        this.startWith = startWith;
        return this;
    }
    
    public BigInteger getIncrementBy() {
        return incrementBy;
    }
    
    public AutoIncrementConstraint setIncrementBy(BigInteger incrementBy) {
        this.incrementBy = incrementBy;
        return this;
    }
}

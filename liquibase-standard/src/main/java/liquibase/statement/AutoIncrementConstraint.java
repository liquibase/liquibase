package liquibase.statement;

import lombok.Getter;

import java.math.BigInteger;

@Getter
public class AutoIncrementConstraint implements ColumnConstraint {
    private String columnName;
    private BigInteger startWith;
    private BigInteger incrementBy;
    private String generationType;
    private Boolean defaultOnNull;
    
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

    public AutoIncrementConstraint(
            String columnName, BigInteger startWith, BigInteger incrementBy, String generationType, Boolean defaultOnNull) {
        this(columnName, startWith, incrementBy);
        setGenerationType(generationType);
        setDefaultOnNull(defaultOnNull);
    }

    public AutoIncrementConstraint setColumnName(String columnName) {
        this.columnName = columnName;
        return this;
    }

    public AutoIncrementConstraint setStartWith(BigInteger startWith) {
        this.startWith = startWith;
        return this;
    }

    public AutoIncrementConstraint setIncrementBy(BigInteger incrementBy) {
        this.incrementBy = incrementBy;
        return this;
    }

    public AutoIncrementConstraint setGenerationType(String generationType) {
        this.generationType = generationType;
        return this;
    }

    public AutoIncrementConstraint setDefaultOnNull(Boolean defaultOnNull) {
        this.defaultOnNull = defaultOnNull;
        return this;
    }
}

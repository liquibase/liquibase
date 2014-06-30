package liquibase.statement;

import liquibase.AbstractExtensibleObject;

import java.math.BigInteger;

/**
 * Describes auto-increment capabilities of a column, used in {@link liquibase.statement.Statement} objects.
 */
public class AutoIncrementConstraint extends AbstractExtensibleObject implements ColumnConstraint {

    private static final String COLUMN_NAME = "columnName";
    private static final String START_WITH = "startWith";
    private static final String INCREMENT_BY = "incrementBy";
    
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
        return getAttribute(COLUMN_NAME, String.class);
    }

    public AutoIncrementConstraint setColumnName(String columnName) {
        return (AutoIncrementConstraint) setAttribute(COLUMN_NAME, columnName);
    }

    public BigInteger getStartWith() {
    	return getAttribute(START_WITH, BigInteger.class);
    }
    
    public AutoIncrementConstraint setStartWith(BigInteger startWith) {
    	return (AutoIncrementConstraint) setAttribute(START_WITH, startWith);
    }
    
    public BigInteger getIncrementBy() {
    	return getAttribute(INCREMENT_BY, BigInteger.class);
    }
    
    public AutoIncrementConstraint setIncrementBy(BigInteger incrementBy) {
    	return (AutoIncrementConstraint) setAttribute(INCREMENT_BY, incrementBy);
    }
}

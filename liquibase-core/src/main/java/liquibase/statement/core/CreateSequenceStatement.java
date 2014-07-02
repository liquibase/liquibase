package liquibase.statement.core;

import liquibase.statement.AbstractStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Sequence;

import java.math.BigInteger;

public class CreateSequenceStatement extends AbstractSequenceStatement {

    private static final String INCREMENT_BY = "incrementBy";
    private static final String MAX_VALUE = "maxValue";
    private static final String MIN_VALUE = "minValue";
    private static final String ORDERED = "ordered";

    private static final String START_VALUE = "startValue";
    private static final String CYCLE = "cycle";
    private static final String CACHE_SIZE = "cacheSize";

    public CreateSequenceStatement() {
    }

    public CreateSequenceStatement(String catalogName, String schemaName, String sequenceName) {
        super(catalogName, schemaName, sequenceName);
    }

    @Override
    public boolean skipOnUnsupported() {
        return true;
    }

    public BigInteger getIncrementBy() {
        return getAttribute(INCREMENT_BY, BigInteger.class);
    }

    public CreateSequenceStatement setIncrementBy(BigInteger incrementBy) {
        return (CreateSequenceStatement) setAttribute(INCREMENT_BY, incrementBy);
    }

    public BigInteger getMaxValue() {
        return getAttribute(MAX_VALUE, BigInteger.class);
    }

    public CreateSequenceStatement setMaxValue(BigInteger maxValue) {
        return (CreateSequenceStatement) setAttribute(MAX_VALUE, maxValue);
    }

    public BigInteger getMinValue() {
        return getAttribute(MIN_VALUE, BigInteger.class);
    }

    public CreateSequenceStatement setMinValue(BigInteger minValue) {
        return (CreateSequenceStatement) setAttribute(MIN_VALUE, minValue);
    }

    public BigInteger getStartValue() {
        return getAttribute(START_VALUE, BigInteger.class);
    }

    public CreateSequenceStatement setStartValue(BigInteger startValue) {
        return (CreateSequenceStatement) setAttribute(START_VALUE, startValue);
    }

    public Boolean getOrdered() {
        return getAttribute(ORDERED, Boolean.class);
    }

    public CreateSequenceStatement setOrdered(Boolean ordered) {
        return (CreateSequenceStatement) setAttribute(ORDERED, ordered);
    }

    public Boolean getCycle() {
        return getAttribute(CYCLE, Boolean.class);
    }

    public CreateSequenceStatement setCycle(Boolean cycle) {
        return (CreateSequenceStatement) setAttribute(CYCLE, cycle);
    }

    public BigInteger getCacheSize() {
        return getAttribute(CACHE_SIZE, BigInteger.class);
    }

    public CreateSequenceStatement setCacheSize(BigInteger cacheSize) {
        return (CreateSequenceStatement) setAttribute(CACHE_SIZE, cacheSize);
    }

    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        return new DatabaseObject[] {
            new Sequence().setName(getSequenceName()).setSchema(getCatalogName(), getSchemaName())
        };
    }
}

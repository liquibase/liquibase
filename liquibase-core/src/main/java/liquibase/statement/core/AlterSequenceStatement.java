package liquibase.statement.core;

import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Sequence;

import java.math.BigInteger;

/**
 * Alters an existing sequence.
 */
public class AlterSequenceStatement extends AbstractSequenceStatement {

    private static final String INCREMENT_BY = "incrementBy";
    private static final String MAX_VALUE = "maxValue";
    private static final String MIN_VALUE = "minValue";
    private static final String ORDERED = "ordered";

    public AlterSequenceStatement() {
    }

    public AlterSequenceStatement(String catalogName, String schemaName, String sequenceName) {
        super(catalogName, schemaName, sequenceName);
    }

    @Override
    public boolean skipOnUnsupported() {
        return true;
    }

    public BigInteger getIncrementBy() {
        return getAttribute(INCREMENT_BY, BigInteger.class);
    }

    public AlterSequenceStatement setIncrementBy(BigInteger incrementBy) {
        return (AlterSequenceStatement) setAttribute(INCREMENT_BY, incrementBy);
    }

    public BigInteger getMaxValue() {
        return getAttribute(MAX_VALUE, BigInteger.class);
    }

    public AlterSequenceStatement setMaxValue(BigInteger maxValue) {
        return (AlterSequenceStatement) setAttribute(MAX_VALUE, maxValue);
    }

    public BigInteger getMinValue() {
        return getAttribute(MIN_VALUE, BigInteger.class);
    }

    public AlterSequenceStatement setMinValue(BigInteger minValue) {
        return (AlterSequenceStatement) setAttribute(MIN_VALUE, minValue);
    }

    public Boolean getOrdered() {
        return getAttribute(ORDERED, Boolean.class);
    }

    public AlterSequenceStatement setOrdered(Boolean ordered) {
        return (AlterSequenceStatement) setAttribute(ORDERED, ordered);
    }

    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        return new DatabaseObject[] {
            new Sequence().setName(getSequenceName()).setSchema(getCatalogName(), getSchemaName())
        };
    }
}

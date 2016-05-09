package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

import java.math.BigInteger;

public class AlterSequenceStatement extends AbstractSqlStatement {

    private String catalogName;
    private String schemaName;
    private String sequenceName;
    private BigInteger incrementBy;
    private BigInteger maxValue;
    private BigInteger minValue;
    private BigInteger cacheSize;
    private Boolean cycle;
    private Boolean ordered;

    public AlterSequenceStatement(String catalogName, String schemaName, String sequenceName) {
        this.catalogName = catalogName;
        this.schemaName = schemaName;
        this.sequenceName = sequenceName;
    }

    @Override
    public boolean skipOnUnsupported() {
        return true;
    }

    public String getCatalogName() {
        return catalogName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getSequenceName() {
        return sequenceName;
    }

    public BigInteger getIncrementBy() {
        return incrementBy;
    }

    public AlterSequenceStatement setIncrementBy(BigInteger incrementBy) {
        this.incrementBy = incrementBy;
        return this;
    }

    public BigInteger getMaxValue() {
        return maxValue;
    }

    public AlterSequenceStatement setMaxValue(BigInteger maxValue) {
        this.maxValue = maxValue;
        return this;
    }

    public BigInteger getMinValue() {
        return minValue;
    }

    public AlterSequenceStatement setMinValue(BigInteger minValue) {
        this.minValue = minValue;
        return this;
    }

    public Boolean getOrdered() {
        return ordered;
    }

    public AlterSequenceStatement setOrdered(Boolean ordered) {
        this.ordered = ordered;
        return this;
    }

    public BigInteger getCacheSize() {
        return cacheSize;
    }

    public AlterSequenceStatement setCacheSize(BigInteger cacheSize) {
        this.cacheSize = cacheSize;
        return this;
    }

    public Boolean getCycle() {
        return cycle;
    }

    public AlterSequenceStatement setCycle(Boolean cycle) {
        this.cycle = cycle;
        return this;
    }
}

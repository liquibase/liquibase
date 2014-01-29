package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

import java.math.BigInteger;

public class CreateSequenceStatement extends AbstractSqlStatement {

    private String catalogName;
    private String schemaName;
    private String sequenceName;
    private BigInteger startValue;
    private BigInteger incrementBy;
    private BigInteger maxValue;
    private BigInteger minValue;
    private Boolean ordered;
    private Boolean cycle;
    private BigInteger cacheSize;

    public CreateSequenceStatement(String catalogName, String schemaName, String sequenceName) {
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

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getSequenceName() {
        return sequenceName;
    }

    public BigInteger getStartValue() {
        return startValue;
    }

    public CreateSequenceStatement setStartValue(BigInteger startValue) {
        this.startValue = startValue;
        return this;
    }

    public BigInteger getIncrementBy() {
        return incrementBy;
    }

    public CreateSequenceStatement setIncrementBy(BigInteger incrementBy) {
        this.incrementBy = incrementBy;
        return this;
    }

    public BigInteger getMaxValue() {
        return maxValue;
    }

    public CreateSequenceStatement setMaxValue(BigInteger maxValue) {
        this.maxValue = maxValue;
        return this;
    }

    public BigInteger getMinValue() {
        return minValue;
    }

    public CreateSequenceStatement setMinValue(BigInteger minValue) {
        this.minValue = minValue;
        return this;
    }

    public Boolean getOrdered() {
        return ordered;
    }

    public CreateSequenceStatement setOrdered(Boolean ordered) {
        this.ordered = ordered;
        return this;
    }

    public Boolean getCycle() {
        return cycle;
    }

    public CreateSequenceStatement setCycle(Boolean cycle) {
        this.cycle = cycle;
        return this;
    }

    public BigInteger getCacheSize() {
        return cacheSize;
    }

    public CreateSequenceStatement setCacheSize(BigInteger cacheSize) {
        this.cacheSize = cacheSize;
        return this;
    }
}

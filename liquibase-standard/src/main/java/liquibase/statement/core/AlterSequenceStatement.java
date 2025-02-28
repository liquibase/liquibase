package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;
import lombok.Getter;

import java.math.BigInteger;

@Getter
public class AlterSequenceStatement extends AbstractSqlStatement {

    private final String catalogName;
    private final String schemaName;
    private final String sequenceName;
    private BigInteger incrementBy;
    private BigInteger maxValue;
    private BigInteger minValue;
    private BigInteger cacheSize;
    private Boolean cycle;
    private Boolean ordered;
    private String dataType;

    public AlterSequenceStatement(String catalogName, String schemaName, String sequenceName) {
        this.catalogName = catalogName;
        this.schemaName = schemaName;
        this.sequenceName = sequenceName;
    }

    @Override
    public boolean skipOnUnsupported() {
        return true;
    }

    public AlterSequenceStatement setIncrementBy(BigInteger incrementBy) {
        this.incrementBy = incrementBy;
        return this;
    }

    public AlterSequenceStatement setMaxValue(BigInteger maxValue) {
        this.maxValue = maxValue;
        return this;
    }

    public AlterSequenceStatement setMinValue(BigInteger minValue) {
        this.minValue = minValue;
        return this;
    }

    public AlterSequenceStatement setOrdered(Boolean ordered) {
        this.ordered = ordered;
        return this;
    }

    public AlterSequenceStatement setCacheSize(BigInteger cacheSize) {
        this.cacheSize = cacheSize;
        return this;
    }

    public AlterSequenceStatement setCycle(Boolean cycle) {
        this.cycle = cycle;
        return this;
    }

    public AlterSequenceStatement setDataType(String dataType) {
        this.dataType = dataType;
        return this;
    }
}

package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;
import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;

@Getter
public class CreateSequenceStatement extends AbstractSqlStatement {

    @Setter
    private String catalogName;
    private final String schemaName;
    private final String sequenceName;
    private BigInteger startValue;
    private BigInteger incrementBy;
    private BigInteger maxValue;
    private BigInteger minValue;
    private Boolean ordered;
    private Boolean cycle;
    private BigInteger cacheSize;
    private String dataType;

    public CreateSequenceStatement(String catalogName, String schemaName, String sequenceName) {
        this.catalogName = catalogName;
        this.schemaName = schemaName;
        this.sequenceName = sequenceName;
    }

    @Override
    public boolean skipOnUnsupported() {
        return true;
    }

    public CreateSequenceStatement setStartValue(BigInteger startValue) {
        this.startValue = startValue;
        return this;
    }

    public CreateSequenceStatement setIncrementBy(BigInteger incrementBy) {
        this.incrementBy = incrementBy;
        return this;
    }

    public CreateSequenceStatement setMaxValue(BigInteger maxValue) {
        this.maxValue = maxValue;
        return this;
    }

    public CreateSequenceStatement setMinValue(BigInteger minValue) {
        this.minValue = minValue;
        return this;
    }

    public CreateSequenceStatement setOrdered(Boolean ordered) {
        this.ordered = ordered;
        return this;
    }

    public CreateSequenceStatement setCycle(Boolean cycle) {
        this.cycle = cycle;
        return this;
    }

    public CreateSequenceStatement setCacheSize(BigInteger cacheSize) {
        this.cacheSize = cacheSize;
        return this;
    }

    public CreateSequenceStatement setDataType(String dataType) {
        this.dataType = dataType;
        return this;
    }
}

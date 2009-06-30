package liquibase.statement.core;

import liquibase.statement.SqlStatement;

public class CreateSequenceStatement implements SqlStatement {

    private String schemaName;
    private String sequenceName;
    private Integer startValue;
    private Integer incrementBy;
    private Integer maxValue;
    private Integer minValue;
    private Boolean ordered;

    public CreateSequenceStatement(String schemaName, String sequenceName) {
        this.schemaName = schemaName;
        this.sequenceName = sequenceName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getSequenceName() {
        return sequenceName;
    }

    public Integer getStartValue() {
        return startValue;
    }

    public CreateSequenceStatement setStartValue(Integer startValue) {
        this.startValue = startValue;
        return this;
    }

    public Integer getIncrementBy() {
        return incrementBy;
    }

    public CreateSequenceStatement setIncrementBy(Integer incrementBy) {
        this.incrementBy = incrementBy;
        return this;
    }

    public Integer getMaxValue() {
        return maxValue;
    }

    public CreateSequenceStatement setMaxValue(Integer maxValue) {
        this.maxValue = maxValue;
        return this;
    }

    public Integer getMinValue() {
        return minValue;
    }

    public CreateSequenceStatement setMinValue(Integer minValue) {
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
}

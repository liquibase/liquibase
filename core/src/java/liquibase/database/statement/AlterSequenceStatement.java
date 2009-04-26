package liquibase.database.statement;

import liquibase.database.*;
import liquibase.exception.StatementNotSupportedOnDatabaseException;

public class AlterSequenceStatement implements SqlStatement {

    private String schemaName;
    private String sequenceName;
    private Integer incrementBy;
    private Integer maxValue;
    private Integer minValue;
    private Boolean ordered;

    public AlterSequenceStatement(String schemaName, String sequenceName) {
        this.schemaName = schemaName;
        this.sequenceName = sequenceName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getSequenceName() {
        return sequenceName;
    }

    public Integer getIncrementBy() {
        return incrementBy;
    }

    public AlterSequenceStatement setIncrementBy(Integer incrementBy) {
        this.incrementBy = incrementBy;
        return this;
    }

    public Integer getMaxValue() {
        return maxValue;
    }

    public AlterSequenceStatement setMaxValue(Integer maxValue) {
        this.maxValue = maxValue;
        return this;
    }

    public Integer getMinValue() {
        return minValue;
    }

    public AlterSequenceStatement setMinValue(Integer minValue) {
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
}

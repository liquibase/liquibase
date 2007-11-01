package liquibase.database.sql;

import liquibase.database.*;
import liquibase.exception.StatementNotSupportedOnDatabaseException;

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

    public String getSqlStatement(Database database) throws StatementNotSupportedOnDatabaseException {
        if (!supportsDatabase(database)) {
            throw new StatementNotSupportedOnDatabaseException(this, database);
        }

        StringBuffer buffer = new StringBuffer();
        buffer.append("CREATE SEQUENCE ");
        buffer.append(database.escapeSequenceName(getSchemaName(), getSequenceName()));
        if (getStartValue() != null) {
            if (database instanceof FirebirdDatabase) {
                throw new StatementNotSupportedOnDatabaseException("Firebird does not support creating sequences with startValue", this, database);
            } else {
                buffer.append(" START WITH ").append(getStartValue());
            }
        }
        if (getIncrementBy() != null) {
            if (database instanceof FirebirdDatabase) {
                throw new StatementNotSupportedOnDatabaseException("Firebird does not support creating sequences with increments", this, database);
            } else {
                buffer.append(" INCREMENT BY ").append(getIncrementBy());
            }
        }
        if (getMinValue() != null) {
            if (database instanceof FirebirdDatabase || database instanceof HsqlDatabase) {
                throw new StatementNotSupportedOnDatabaseException("Database does not support creating sequences with minValue", this, database);
            } else {
                buffer.append(" MINVALUE ").append(getMinValue());
            }
        }
        if (getMaxValue() != null) {
            if (database instanceof FirebirdDatabase || database instanceof HsqlDatabase) {
                throw new StatementNotSupportedOnDatabaseException("Database does not support creating sequences with maxValue", this, database);
            } else {
                buffer.append(" MAXVALUE ").append(getMaxValue());
            }
        }

        if (getOrdered() != null) {
            if (database instanceof OracleDatabase || database instanceof DB2Database) {
                if (getOrdered()) {
                    buffer.append(" ORDER");
                }
            } else {
                throw new StatementNotSupportedOnDatabaseException("Database does not support creating sequences with 'order'", this, database);
            }
        }

        return buffer.toString();
    }

    public String getEndDelimiter(Database database) {
        return ";";
    }

    public boolean supportsDatabase(Database database) {
        return database.supportsSequences();
    }
}

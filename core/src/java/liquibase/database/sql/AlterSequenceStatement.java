package liquibase.database.sql;

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

    public String getSqlStatement(Database database) throws StatementNotSupportedOnDatabaseException {
        if (!supportsDatabase(database)) {
            throw new StatementNotSupportedOnDatabaseException(this, database);
        }

        StringBuffer buffer = new StringBuffer();
        buffer.append("ALTER SEQUENCE ");
        buffer.append(getSequenceName());

        if (getIncrementBy() != null) {
            if (database instanceof FirebirdDatabase || database instanceof HsqlDatabase) {
                throw new StatementNotSupportedOnDatabaseException("Database does not support altering sequences with increment", this, database);
            } else {
                buffer.append(" INCREMENT BY ").append(getIncrementBy());
            }
        }
        if (getMinValue() != null) {
            if (database instanceof FirebirdDatabase || database instanceof HsqlDatabase) {
                buffer.append(" RESTART WITH ").append(getMinValue());
            } else {
                buffer.append(" MINVALUE ").append(getMinValue());
            }
        }

        if (getMaxValue() != null) {
            if (database instanceof FirebirdDatabase || database instanceof HsqlDatabase) {
                throw new StatementNotSupportedOnDatabaseException("Database does not support altering sequences with maxValue", this, database);
            } else {
                buffer.append(" MAXVALUE ").append(getMaxValue());
            }
        }

        if (getOrdered() != null) {
            if (database instanceof OracleDatabase || database instanceof DB2Database || database instanceof MaxDBDatabase) {
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

package liquibase.database.sql;

import liquibase.database.*;
import liquibase.exception.StatementNotSupportedOnDatabaseException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class AddColumnStatement implements SqlStatement {

    private String schemaName;
    private String tableName;
    private String columnName;
    private String columnType;
    private Object defaultValue;
    private Set<ColumnConstraint> constraints = new HashSet<ColumnConstraint>();

    public AddColumnStatement(String schemaName, String tableName, String columnName, String columnType, Object defaultValue, ColumnConstraint... constraints) {
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.columnName = columnName;
        this.columnType = columnType;
        this.defaultValue = defaultValue;
        if (constraints != null) {
            this.constraints.addAll(Arrays.asList(constraints));
        }
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getTableName() {
        return tableName;
    }

    public String getColumnName() {
        return columnName;
    }

    public String getColumnType() {
        return columnType;
    }

    public Set<ColumnConstraint> getConstraints() {
        return constraints;
    }

    public String getSqlStatement(Database database) throws StatementNotSupportedOnDatabaseException {
        if (isPrimaryKey() && (database instanceof CacheDatabase
                || database instanceof H2Database
                || database instanceof DB2Database
                || database instanceof DerbyDatabase)) {
            throw new StatementNotSupportedOnDatabaseException("Adding primary key columns is not supported", this, database);
        }

        String alterTable = "ALTER TABLE " + database.escapeTableName(getSchemaName(), getTableName()) + " ADD "+getColumnName() + " " + database.getColumnType(getColumnType(), isAutoIncrement());

        if (defaultClauseBeforeNotNull(database)) {
            alterTable += getDefaultClause(database);
        }

        if (primaryKeyBeforeNotNull(database)) {
            if (isPrimaryKey()) {
                alterTable += " PRIMARY KEY";
            }
        }

        if (isAutoIncrement()) {
            alterTable += " "+database.getAutoIncrementClause();
        }

        if (!isNullable()) {
            alterTable += " NOT NULL";
        } else {
            if (database instanceof SybaseDatabase) {
                alterTable += " NULL";
            }
        }

        if (!primaryKeyBeforeNotNull(database)) {
            if (isPrimaryKey()) {
                alterTable += " PRIMARY KEY";
            }
        }
        
        if (!defaultClauseBeforeNotNull(database)) {
            alterTable += getDefaultClause(database);
        }

        return alterTable;
    }

    private boolean primaryKeyBeforeNotNull(Database database) {
        return !(database instanceof HsqlDatabase);
    }

    private boolean isAutoIncrement() {
        for (ColumnConstraint constraint : getConstraints()) {
            if (constraint instanceof AutoIncrementConstraint) {
                return true;
            }
        }
        return false;
    }

    public boolean isPrimaryKey() {
        for (ColumnConstraint constraint : getConstraints()) {
            if (constraint instanceof PrimaryKeyConstraint) {
                return true;
            }
        }
        return false;
    }

    public String getEndDelimiter(Database database) {
        return ";";
    }

    public boolean supportsDatabase(Database database) {
        return true;
    }

    private boolean defaultClauseBeforeNotNull(Database database) {
        return database instanceof OracleDatabase
                || database instanceof HsqlDatabase
                || database instanceof DerbyDatabase
                || database instanceof DB2Database
                || database instanceof FirebirdDatabase;
    }

    private String getDefaultClause(Database database) {
        String clause = "";
        if (getDefaultValue() != null) {
            if (database instanceof MSSQLDatabase) {
                clause += " CONSTRAINT " + ((MSSQLDatabase) database).generateDefaultConstraintName(tableName, getColumnName());
            }
            clause += " DEFAULT " + database.convertJavaObjectToString(getDefaultValue());
        }
        return clause;
    }

    public boolean isNullable() {
        if (isPrimaryKey()) {
            return false;
        }
        for (ColumnConstraint constraint : getConstraints()) {
            if (constraint instanceof NotNullConstraint) {
                return false;
            }
        }
        return true;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }
}
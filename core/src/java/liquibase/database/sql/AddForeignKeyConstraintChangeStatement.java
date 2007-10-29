package liquibase.database.sql;

import liquibase.database.Database;
import liquibase.exception.StatementNotSupportedOnDatabaseException;

public class AddForeignKeyConstraintChangeStatement implements SqlStatement {

    private String baseTableSchemaName;
    private String baseTableName;
    private String baseColumnNames;

    private String referencedTableSchemaName;
    private String referencedTableName;
    private String referencedColumnNames;

    private String constraintName;

    private boolean deferrable;
    private boolean initiallyDeferred;

    private boolean deleteCascade;

    public AddForeignKeyConstraintChangeStatement(String constraintName, String baseTableSchemaName, String baseTableName, String baseColumnNames, String referencedTableSchemaName, String referencedTableName, String referencedColumnNames) {
        this.baseTableSchemaName = baseTableSchemaName;
        this.baseTableName = baseTableName;
        this.baseColumnNames = baseColumnNames;
        this.referencedTableSchemaName = referencedTableSchemaName;
        this.referencedTableName = referencedTableName;
        this.referencedColumnNames = referencedColumnNames;
        this.constraintName = constraintName;
    }

    public String getBaseTableSchemaName() {
        return baseTableSchemaName;
    }

    public String getBaseTableName() {
        return baseTableName;
    }

    public String getBaseColumnNames() {
        return baseColumnNames;
    }

    public String getReferencedTableSchemaName() {
        return referencedTableSchemaName;
    }

    public String getReferencedTableName() {
        return referencedTableName;
    }

    public String getReferencedColumnNames() {
        return referencedColumnNames;
    }

    public String getConstraintName() {
        return constraintName;
    }

    public boolean isDeferrable() {
        return deferrable;
    }

    public void setDeferrable(boolean deferrable) {
        this.deferrable = deferrable;
    }

    public boolean isInitiallyDeferred() {
        return initiallyDeferred;
    }

    public void setInitiallyDeferred(boolean initiallyDeferred) {
        this.initiallyDeferred = initiallyDeferred;
    }

    public boolean isDeleteCascade() {
        return deleteCascade;
    }

    public void setDeleteCascade(boolean deleteCascade) {
        this.deleteCascade = deleteCascade;
    }

    public String getSqlStatement(Database database) throws StatementNotSupportedOnDatabaseException {
        String sql = "ALTER TABLE " + database.escapeTableName(getBaseTableSchemaName(), getBaseTableName()) + " ADD CONSTRAINT " + getConstraintName() + " FOREIGN KEY (" + getBaseColumnNames() + ") REFERENCES " + database.escapeTableName(getReferencedTableSchemaName(), getReferencedTableName()) + "(" + getReferencedColumnNames() + ")";

        if (isDeleteCascade()) {
            sql += " ON DELETE CASCADE";
        }

        if (database.supportsInitiallyDeferrableColumns()) {
            if (isDeferrable()) {
                sql += " DEFERRABLE";
            }

            if (isInitiallyDeferred()) {
                sql += " INITIALLY DEFERRED";
            }
        }

        return sql;
    }

    public String getEndDelimiter(Database database) {
        return ";";
    }

    public boolean supportsDatabase(Database database) {
        return true;
    }
}

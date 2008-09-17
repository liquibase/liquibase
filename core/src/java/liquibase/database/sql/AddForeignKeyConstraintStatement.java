package liquibase.database.sql;

import java.sql.DatabaseMetaData;

import liquibase.database.Database;
import liquibase.database.SQLiteDatabase;
import liquibase.database.OracleDatabase;
import liquibase.exception.StatementNotSupportedOnDatabaseException;

public class AddForeignKeyConstraintStatement implements SqlStatement {

    private String baseTableSchemaName;
    private String baseTableName;
    private String baseColumnNames;

    private String referencedTableSchemaName;
    private String referencedTableName;
    private String referencedColumnNames;

    private String constraintName;

    private boolean deferrable;
    private boolean initiallyDeferred;

    private Integer deleteRule;
    private Integer updateRule;

    public AddForeignKeyConstraintStatement(String constraintName, String baseTableSchemaName, String baseTableName, String baseColumnNames, String referencedTableSchemaName, String referencedTableName, String referencedColumnNames) {
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

    public Integer getDeleteRule() {
        return deleteRule;
    }

    public Integer getUpdateRule() {
        return updateRule;
    }

    public AddForeignKeyConstraintStatement setDeferrable(boolean deferrable) {
        this.deferrable = deferrable;
        return this;
    }

    public boolean isInitiallyDeferred() {
        return initiallyDeferred;
    }

    public AddForeignKeyConstraintStatement setInitiallyDeferred(boolean initiallyDeferred) {
        this.initiallyDeferred = initiallyDeferred;
        return this;
    }

    public AddForeignKeyConstraintStatement setUpdateRule(Integer updateRule) {
        this.updateRule = updateRule;
        return this;
    }

    public AddForeignKeyConstraintStatement setDeleteRule(Integer deleteRule) {
        this.deleteRule = deleteRule;
        return this;
    }

    public String getSqlStatement(Database database) throws StatementNotSupportedOnDatabaseException {
        if (!supportsDatabase(database)) {
            throw new StatementNotSupportedOnDatabaseException(this, database);
        }

        String sql = "ALTER TABLE " + database.escapeTableName(getBaseTableSchemaName(), getBaseTableName()) + " ADD CONSTRAINT " + getConstraintName() + " FOREIGN KEY (" + database.escapeColumnNameList(getBaseColumnNames()) + ") REFERENCES " + database.escapeTableName(getReferencedTableSchemaName(), getReferencedTableName()) + "(" + database.escapeColumnNameList(getReferencedColumnNames()) + ")";

        if (this.updateRule != null) {
            switch (this.updateRule) {
                case DatabaseMetaData.importedKeyCascade:
                    sql += " ON UPDATE CASCADE";
                    break;
                case DatabaseMetaData.importedKeySetNull:
                    sql += " ON UPDATE SET NULL";
                    break;
                case DatabaseMetaData.importedKeySetDefault:
                    sql += " ON UPDATE SET DEFAULT";
                    break;
                case DatabaseMetaData.importedKeyRestrict:
                    if (database.supportsRestrictForeignKeys()) {
                        sql += " ON UPDATE RESTRICT";
                    }
                    break;
                case DatabaseMetaData.importedKeyNoAction:
                    //don't do anything
//                    sql += " ON UPDATE NO ACTION";
                    break;
                default:
                    break;
            }
        }
        if (this.deleteRule != null) {
            switch (this.deleteRule) {
                case DatabaseMetaData.importedKeyCascade:
                    sql += " ON DELETE CASCADE";
                    break;
                case DatabaseMetaData.importedKeySetNull:
                    sql += " ON DELETE SET NULL";
                    break;
                case DatabaseMetaData.importedKeySetDefault:
                    sql += " ON DELETE SET DEFAULT";
                    break;
                case DatabaseMetaData.importedKeyRestrict:
                    if (database.supportsRestrictForeignKeys()) {
                        sql += " ON DELETE RESTRICT";
                    }
                    break;
                case DatabaseMetaData.importedKeyNoAction:
                    //don't do anything
//                    sql += " ON DELETE NO ACTION";
                    break;
                default:
                    break;
            }
        }

        if (isDeferrable() || isInitiallyDeferred()) {
            if (!database.supportsInitiallyDeferrableColumns()) {
                throw new StatementNotSupportedOnDatabaseException("Database does not support deferrable foreign keys", this, database);
            }

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
        return (!(database instanceof SQLiteDatabase));
    }


}

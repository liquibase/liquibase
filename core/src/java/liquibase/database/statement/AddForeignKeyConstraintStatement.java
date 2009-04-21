package liquibase.database.statement;

import java.sql.DatabaseMetaData;

import liquibase.database.Database;
import liquibase.database.InformixDatabase;
import liquibase.database.SQLiteDatabase;
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
        
        StringBuilder sb = new StringBuilder();
        sb.append("ALTER TABLE ")
        	.append(database.escapeTableName(getBaseTableSchemaName(), getBaseTableName()))
        	.append(" ADD CONSTRAINT ");
        if (!(database instanceof InformixDatabase)) {
        	sb.append(database.escapeConstraintName(getConstraintName()));
        }
        sb.append(" FOREIGN KEY (")
        	.append(database.escapeColumnNameList(getBaseColumnNames()))
        	.append(") REFERENCES ")
        	.append(database.escapeTableName(getReferencedTableSchemaName(), getReferencedTableName()))
        	.append("(")
        	.append(database.escapeColumnNameList(getReferencedColumnNames()))
        	.append(")");

        if (this.updateRule != null) {
            switch (this.updateRule) {
                case DatabaseMetaData.importedKeyCascade:
                    sb.append(" ON UPDATE CASCADE");
                    break;
                case DatabaseMetaData.importedKeySetNull:
                    sb.append(" ON UPDATE SET NULL");
                    break;
                case DatabaseMetaData.importedKeySetDefault:
                    sb.append(" ON UPDATE SET DEFAULT");
                    break;
                case DatabaseMetaData.importedKeyRestrict:
                    if (database.supportsRestrictForeignKeys()) {
                        sb.append(" ON UPDATE RESTRICT");
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
                    sb.append(" ON DELETE CASCADE");
                    break;
                case DatabaseMetaData.importedKeySetNull:
                	sb.append(" ON DELETE SET NULL");
                    break;
                case DatabaseMetaData.importedKeySetDefault:
                	sb.append(" ON DELETE SET DEFAULT");
                    break;
                case DatabaseMetaData.importedKeyRestrict:
                    if (database.supportsRestrictForeignKeys()) {
                    	sb.append(" ON DELETE RESTRICT");
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
            	sb.append(" DEFERRABLE");
            }

            if (isInitiallyDeferred()) {
            	sb.append(" INITIALLY DEFERRED");
            }
        }
        
        if (database instanceof InformixDatabase) {
        	sb.append(" CONSTRAINT ");
        	sb.append(database.escapeConstraintName(getConstraintName()));
        }

        return sb.toString();
    }

    public String getEndDelimiter(Database database) {
        return ";";
    }

    public boolean supportsDatabase(Database database) {
        return (!(database instanceof SQLiteDatabase));
    }


}

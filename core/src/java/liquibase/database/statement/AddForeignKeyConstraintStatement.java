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
}

package liquibase.change.core;

import liquibase.change.AbstractChange;
import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.database.Database;
import liquibase.database.core.OracleDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.RenamePrimaryKeyStatement;

/**
 * Removes an existing primary key.
 */
@DatabaseChange(name="renamePrimaryKey", description = "Renames an existing primary key", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "primaryKey")
public class RenamePrimaryKeyChange extends AbstractChange {
    private String catalogName;
    private String schemaName;
    private String tableName;
    private String oldConstraintName;
    private String newConstraintName;

    @Override
    public boolean generateStatementsVolatile(Database database) {
        // TODO just these are supported for now
        return (database instanceof PostgresDatabase) || (database instanceof OracleDatabase); 
    }

    @DatabaseChangeProperty(mustEqualExisting ="primaryKey.catalog")
    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    @DatabaseChangeProperty(mustEqualExisting ="primaryKey.schema")
    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "primaryKey.table", description = "Name of the table to rename the primary key of")
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "primaryKey", description = "Old name of the primary key")
    public String getOldConstraintName() {
        return oldConstraintName;
    }

    public void setOldConstraintName(String oldConstraintName) {
        this.oldConstraintName = oldConstraintName;
    }

    @DatabaseChangeProperty(description = "New name for the primary key")
    public String getNewConstraintName() {
      return newConstraintName;
    }
  
    public void setNewConstraintName(String newConstraintName) {
        this.newConstraintName = newConstraintName;
    }
  
    @Override
    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[]{
                new RenamePrimaryKeyStatement(getCatalogName(), getSchemaName(), getTableName(), getOldConstraintName(), getNewConstraintName()),
        };
    }
    
    @Override
    public String getConfirmationMessage() {
        return "Primary key of table " + getTableName() + " renamed to " + getNewConstraintName();
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
}

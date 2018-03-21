package liquibase.change.core;

import liquibase.change.*;
import liquibase.database.Database;
import liquibase.database.core.SQLiteDatabase;
import liquibase.database.core.SQLiteDatabase.AlterTableVisitor;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.DropPrimaryKeyStatement;
import liquibase.structure.core.Index;
import liquibase.structure.core.PrimaryKey;

import java.util.ArrayList;
import java.util.List;

/**
 * Removes an existing primary key.
 */
@DatabaseChange(name="dropPrimaryKey", description = "Drops an existing primary key", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "primaryKey")
public class DropPrimaryKeyChange extends AbstractChange {
    private String catalogName;
    private String schemaName;
    private String tableName;
    private String constraintName;
    private Boolean dropIndex;

    @Override
    public boolean generateStatementsVolatile(Database database) {
        if (database instanceof SQLiteDatabase) {
            return true;
        }
        return false;
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

    @DatabaseChangeProperty(mustEqualExisting = "primaryKey.table", description = "Name of the table to drop the primary key of")
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "primaryKey", description = "Name of the primary key")
    public String getConstraintName() {
        return constraintName;
    }

    public void setConstraintName(String constraintName) {
        this.constraintName = constraintName;
    }

    public Boolean getDropIndex() {
        return dropIndex;
    }

    public void setDropIndex(Boolean dropIndex) {
        this.dropIndex = dropIndex;
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {

        if (database instanceof SQLiteDatabase) {
    		// return special statements for SQLite databases
    		return generateStatementsForSQLiteDatabase(database);
        }

        DropPrimaryKeyStatement statement = new DropPrimaryKeyStatement(getCatalogName(), getSchemaName(), getTableName(), getConstraintName());
        statement.setDropIndex(this.dropIndex);
        return new SqlStatement[]{
                statement,
        };
    }

    @Override
    public ChangeStatus checkStatus(Database database) {
        try {
            return new ChangeStatus().assertComplete(!SnapshotGeneratorFactory.getInstance().has(new PrimaryKey(getConstraintName(), getCatalogName(), getSchemaName(), getTableName()), database), "Primary key exists");
        } catch (Exception e) {
            return new ChangeStatus().unknown(e);
        }

    }

    private SqlStatement[] generateStatementsForSQLiteDatabase(Database database) {
        SqlStatement[] sqlStatements = null;
        /*nolgpl implement */
        return sqlStatements;
    }

    @Override
    public String getConfirmationMessage() {
        return "Primary key dropped from "+getTableName();
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
}

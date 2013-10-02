package liquibase.change.core;

import liquibase.change.AbstractChange;
import liquibase.change.DatabaseChange;
import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChangeProperty;
import liquibase.database.Database;
import liquibase.database.core.SQLiteDatabase;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.DropForeignKeyConstraintStatement;

/**
 * Drops an existing foreign key constraint.
 */
@DatabaseChange(name="dropForeignKeyConstraint", description = "Drops an existing foreign key", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "foreignKey")
public class DropForeignKeyConstraintChange extends AbstractChange {
    private String baseTableCatalogName;
    private String baseTableSchemaName;
    private String baseTableName;
    private String constraintName;

    @DatabaseChangeProperty(mustEqualExisting ="foreignKey.table.catalog", since = "3.0")
    public String getBaseTableCatalogName() {
        return baseTableCatalogName;
    }

    public void setBaseTableCatalogName(String baseTableCatalogName) {
        this.baseTableCatalogName = baseTableCatalogName;
    }

    @DatabaseChangeProperty(mustEqualExisting ="foreignKey.table.schema")
    public String getBaseTableSchemaName() {
        return baseTableSchemaName;
    }

    public void setBaseTableSchemaName(String baseTableSchemaName) {
        this.baseTableSchemaName = baseTableSchemaName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "foreignKey.table", description = "Name of the table containing the column constrained by the foreign key")
    public String getBaseTableName() {
        return baseTableName;
    }

    public void setBaseTableName(String baseTableName) {
        this.baseTableName = baseTableName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "foreignKey", description = "Name of the foreign key constraint to drop", exampleValue = "fk_address_person")
    public String getConstraintName() {
        return constraintName;
    }

    public void setConstraintName(String constraintName) {
        this.constraintName = constraintName;
    }

    public SqlStatement[] generateStatements(Database database) {

        if (database instanceof SQLiteDatabase) {
    		// return special statements for SQLite databases
    		return generateStatementsForSQLiteDatabase();
    	} 
    	
        return new SqlStatement[]{
                new DropForeignKeyConstraintStatement(
                        getBaseTableCatalogName(),
                        getBaseTableSchemaName(),
                        getBaseTableName(),
                        getConstraintName()),
        };    	
    }
    
    private SqlStatement[] generateStatementsForSQLiteDatabase() {
    	// SQLite does not support foreign keys until now.
		// See for more information: http://www.sqlite.org/omitted.html
		// Therefore this is an empty operation...
		return new SqlStatement[]{};
    }

    public String getConfirmationMessage() {
        return "Foreign key " + getConstraintName() + " dropped";
    }
}

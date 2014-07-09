package liquibase.change.core;

import liquibase.change.*;
import liquibase.database.core.SQLiteDatabase;
import  liquibase.ExecutionEnvironment;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.Statement;
import liquibase.statement.core.DropForeignKeyConstraintStatement;
import liquibase.structure.core.ForeignKey;

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

    @Override
    public Statement[] generateStatements(ExecutionEnvironment env) {

        if (env.getTargetDatabase() instanceof SQLiteDatabase) {
    		// return special statements for SQLite databases
    		return generateStatementsForSQLiteDatabase();
    	} 
    	
        return new Statement[]{
                new DropForeignKeyConstraintStatement(
                        getConstraintName(),
                        getBaseTableCatalogName(),
                        getBaseTableSchemaName(),
                        getBaseTableName()
                )

        };    	
    }

    @Override
    public ChangeStatus checkStatus(ExecutionEnvironment env) {
        try {
            return new ChangeStatus().assertComplete(!SnapshotGeneratorFactory.getInstance().has(new ForeignKey(getConstraintName(), getBaseTableCatalogName(), getBaseTableSchemaName(), getBaseTableCatalogName()), env.getTargetDatabase()), "Foreign key exists");
        } catch (Exception e) {
            return new ChangeStatus().unknown(e);
        }
    }

    private Statement[] generateStatementsForSQLiteDatabase() {
    	// SQLite does not support foreign keys until now.
		// See for more information: http://www.sqlite.org/omitted.html
		// Therefore this is an empty operation...
		return new Statement[]{};
    }

    @Override
    public String getConfirmationMessage() {
        return "Foreign key " + getConstraintName() + " dropped";
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
}

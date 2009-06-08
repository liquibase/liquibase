package liquibase.change.core;

import liquibase.database.Database;
import liquibase.database.SQLiteDatabase;
import liquibase.statement.DropForeignKeyConstraintStatement;
import liquibase.statement.SqlStatement;
import liquibase.change.AbstractChange;
import liquibase.change.ChangeMetaData;

/**
 * Drops an existing foreign key constraint.
 */
public class DropForeignKeyConstraintChange extends AbstractChange {
    private String baseTableSchemaName;
    private String baseTableName;
    private String constraintName;

    public DropForeignKeyConstraintChange() {
        super("dropForeignKeyConstraint", "Drop Foreign Key Constraint", ChangeMetaData.PRIORITY_DEFAULT);
    }

    public String getBaseTableSchemaName() {
        return baseTableSchemaName;
    }

    public void setBaseTableSchemaName(String baseTableSchemaName) {
        this.baseTableSchemaName = baseTableSchemaName;
    }

    public String getBaseTableName() {
        return baseTableName;
    }

    public void setBaseTableName(String baseTableName) {
        this.baseTableName = baseTableName;
    }

    public String getConstraintName() {
        return constraintName;
    }

    public void setConstraintName(String constraintName) {
        this.constraintName = constraintName;
    }

    public SqlStatement[] generateStatements(Database database) {
    	
    	if (database instanceof SQLiteDatabase) {
    		// return special statements for SQLite databases
    		return generateStatementsForSQLiteDatabase(database);
    	} 
    	
        return new SqlStatement[]{
                new DropForeignKeyConstraintStatement(
                        getBaseTableSchemaName() == null?database.getDefaultSchemaName():getBaseTableSchemaName(),
                        getBaseTableName(),
                        getConstraintName()),
        };    	
    }
    
    private SqlStatement[] generateStatementsForSQLiteDatabase(Database database) {
    	// SQLite does not support foreign keys until now.
		// See for more information: http://www.sqlite.org/omitted.html
		// Therefore this is an empty operation...
		return new SqlStatement[]{};
    }

    public String getConfirmationMessage() {
        return "Foreign key " + getConstraintName() + " dropped";
    }
}

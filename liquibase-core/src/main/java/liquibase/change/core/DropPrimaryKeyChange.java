package liquibase.change.core;

import liquibase.change.*;
import liquibase.database.Database;
import liquibase.database.core.SQLiteDatabase;
import liquibase.database.core.SQLiteDatabase.AlterTableVisitor;
import liquibase.database.structure.Index;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.DropPrimaryKeyStatement;

import java.util.ArrayList;
import java.util.List;

/**
 * Removes an existing primary key.
 */
@DatabaseChange(name="dropPrimaryKey", description = "Drop Primary Key", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "primaryKey")
public class DropPrimaryKeyChange extends AbstractChange {
    private String catalogName;
    private String schemaName;
    private String tableName;
    private String constraintName;

    @DatabaseChangeProperty(mustApplyTo ="primaryKey.catalog")
    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    @DatabaseChangeProperty(mustApplyTo ="primaryKey.schema")
    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    @DatabaseChangeProperty(requiredForDatabase = "all", mustApplyTo = "primaryKey.table")
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @DatabaseChangeProperty(requiredForDatabase = "all", mustApplyTo = "primaryKey")
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
                new DropPrimaryKeyStatement(getCatalogName(), getSchemaName(), getTableName(), getConstraintName()),
        };
    }
    
    private SqlStatement[] generateStatementsForSQLiteDatabase(Database database) {
    	
    	// SQLite does not support this ALTER TABLE operation until now.
		// For more information see: http://www.sqlite.org/omitted.html.
		// This is a small work around...
    	
    	// Note: The attribute "constraintName" is used to pass the column 
    	// name instead of the constraint name.
		
    	List<SqlStatement> statements = new ArrayList<SqlStatement>();
    	
		// define alter table logic
		AlterTableVisitor rename_alter_visitor = new AlterTableVisitor() {
			public ColumnConfig[] getColumnsToAdd() {
				return new ColumnConfig[0];
			}
			public boolean copyThisColumn(ColumnConfig column) {
				return true;
			}
			public boolean createThisColumn(ColumnConfig column) {
				if (column.getName().equals(getConstraintName())) {
					column.getConstraints().setPrimaryKey(false);
				}
				return true;
			}
			public boolean createThisIndex(Index index) {
				return true;
			}
		};
    		
    	try {
    		// alter table
			statements.addAll(SQLiteDatabase.getAlterTableStatements(
					rename_alter_visitor,
					database,getCatalogName(), getSchemaName(),getTableName()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return statements.toArray(new SqlStatement[statements.size()]);
    }

    public String getConfirmationMessage() {
        return "Primary key dropped from "+getTableName();
    }
}

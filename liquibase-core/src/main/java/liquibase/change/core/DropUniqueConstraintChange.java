package liquibase.change.core;

import java.util.ArrayList;
import java.util.List;

import liquibase.change.AbstractChange;
import liquibase.change.ChangeClass;
import liquibase.change.ChangeMetaData;
import liquibase.change.ChangeProperty;
import liquibase.change.ColumnConfig;
import liquibase.database.Database;
import liquibase.database.core.SQLiteDatabase;
import liquibase.database.core.SQLiteDatabase.AlterTableVisitor;
import liquibase.database.core.SybaseASADatabase;
import liquibase.database.structure.Index;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.DropUniqueConstraintStatement;

/**
 * Removes an existing unique constraint.
 */
@ChangeClass(name="dropUniqueConstraint", description = "Drop Unique Constraint", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "uniqueConstraint")
public class DropUniqueConstraintChange extends AbstractChange {
    private String catalogName;
    private String schemaName;
    private String tableName;
    private String constraintName;
    /**
     * Sybase ASA does drop unique constraint not by name, but using list of the columns in unique clause.
     */
    private String uniqueColumns;

    @ChangeProperty(mustApplyTo ="uniqueConstraint.table.catalog")
    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    @ChangeProperty(mustApplyTo ="uniqueConstraint.table.schema")
    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    @ChangeProperty(requiredForDatabase = "all", mustApplyTo = "uniqueConstraint.table")
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @ChangeProperty(requiredForDatabase = "all", mustApplyTo = "uniqueConstraint")
    public String getConstraintName() {
        return constraintName;
    }

    public void setConstraintName(String constraintName) {
        this.constraintName = constraintName;
    }

    public String getUniqueColumns() {
		return uniqueColumns;
	}

	public void setUniqueColumns(String uniqueColumns) {
		this.uniqueColumns = uniqueColumns;
	}

    public SqlStatement[] generateStatements(Database database) {
        
//todo    	if (database instanceof SQLiteDatabase) {
//    		// return special statements for SQLite databases
//    		return generateStatementsForSQLiteDatabase(database);
//        }
    	DropUniqueConstraintStatement statement = new DropUniqueConstraintStatement(getCatalogName(), getSchemaName(), getTableName(), getConstraintName());
    	if (database instanceof SybaseASADatabase) {
    		statement.setUniqueColumns(uniqueColumns);
    	}
    	return new SqlStatement[]{
			statement
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
    				column.getConstraints().setUnique(false);            					
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
        return "Unique constraint "+getConstraintName()+" dropped from "+getTableName();
    }
}

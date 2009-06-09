package liquibase.change.core;

import liquibase.database.Database;
import liquibase.database.core.SQLiteDatabase;
import liquibase.database.core.SQLiteDatabase.AlterTableVisitor;
import liquibase.database.structure.Index;
import liquibase.statement.AddUniqueConstraintStatement;
import liquibase.statement.SqlStatement;
import liquibase.util.StringUtils;
import liquibase.change.AbstractChange;
import liquibase.change.ChangeMetaData;
import liquibase.change.ColumnConfig;
import liquibase.change.Change;

import java.util.ArrayList;
import java.util.List;

/**
 * Adds a unique constraint to an existing column.
 */
public class AddUniqueConstraintChange extends AbstractChange {

    private String schemaName;
    private String tableName;
    private String columnNames;
    private String constraintName;
    private String tablespace;

    public AddUniqueConstraintChange() {
        super("addUniqueConstraint", "Add Unique Constraint", ChangeMetaData.PRIORITY_DEFAULT);
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = StringUtils.trimToNull(schemaName);
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(String columnNames) {
        this.columnNames = columnNames;
    }

    public String getConstraintName() {
        return constraintName;
    }

    public void setConstraintName(String constraintName) {
        this.constraintName = constraintName;
    }


    public String getTablespace() {
        return tablespace;
    }

    public void setTablespace(String tablespace) {
        this.tablespace = tablespace;
    }

    public SqlStatement[] generateStatements(Database database) {
        
//todo    	if (database instanceof SQLiteDatabase) {
//    		// return special statements for SQLite databases
//    		return generateStatementsForSQLiteDatabase(database);
//        }
    	
    	AddUniqueConstraintStatement statement = new AddUniqueConstraintStatement(getSchemaName() == null?database.getDefaultSchemaName():getSchemaName(), getTableName(), getColumnNames(), getConstraintName());
        statement.setTablespace(getTablespace());

        return new SqlStatement[] { statement };
    }
    
    private SqlStatement[] generateStatementsForSQLiteDatabase(Database database) {
    	
    	// SQLite does not support this ALTER TABLE operation until now.
		// For more information see: http://www.sqlite.org/omitted.html.
		// This is a small work around...
    	
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
				String[] split_columns = getColumnNames().split("[ ]*,[ ]*");
				for (String split_column:split_columns) {
					if (column.getName().equals(split_column)) {
    					column.getConstraints().setUnique(true);            					
    				}
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
					database,getSchemaName(),getTableName()));
    	} catch (Exception e) {
			e.printStackTrace();
		}
    	
    	return statements.toArray(new SqlStatement[statements.size()]);
    }

    public String getConfirmationMessage() {
        return "Unique constraint added to "+getTableName()+"("+getColumnNames()+")";
    }

    @Override
    protected Change[] createInverses() {
        DropUniqueConstraintChange inverse = new DropUniqueConstraintChange();
        inverse.setSchemaName(getSchemaName());
        inverse.setTableName(getTableName());
        inverse.setConstraintName(getConstraintName());

        return new Change[]{
                inverse,
        };
    }
}

package liquibase.change.core;

import liquibase.change.*;
import liquibase.database.Database;
import liquibase.database.core.DerbyDatabase;
import liquibase.database.core.SQLiteDatabase;
import liquibase.database.core.SQLiteDatabase.AlterTableVisitor;
import  liquibase.ExecutionEnvironment;
import liquibase.statement.Statement;
import liquibase.statement.core.RawSqlStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.Index;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Combines data from two existing columns into a new column and drops the original columns.
 */
@DatabaseChange(name="mergeColumns", description = "Concatenates the values in two columns, joins them by with string, and stores the resulting value in a new column.", priority = ChangeMetaData.PRIORITY_DEFAULT)
public class MergeColumnChange extends AbstractChange {

    private String catalogName;
    private String schemaName;
    private String tableName;
    private String column1Name;
    private String joinString;
    private String column2Name;
    private String finalColumnName;
    private String finalColumnType;

    @Override
    public boolean supports(ExecutionEnvironment env) {
        return super.supports(env) && !(env.getTargetDatabase() instanceof DerbyDatabase);
    }

    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    @DatabaseChangeProperty(description = "Name of the table containing the columns to join")
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @DatabaseChangeProperty(description = "Name of the column containing the first half of the data", exampleValue = "first_name")
    public String getColumn1Name() {
        return column1Name;
    }

    public void setColumn1Name(String column1Name) {
        this.column1Name = column1Name;
    }

    @DatabaseChangeProperty(description = "String to place include between the values from column1 and column2 (may be empty)", exampleValue = " ")
    public String getJoinString() {
        return joinString;
    }

    public void setJoinString(String joinString) {
        this.joinString = joinString;
    }

    @DatabaseChangeProperty(description = "Name of the column containing the second half of the data", exampleValue = "last_name")
    public String getColumn2Name() {
        return column2Name;
    }

    public void setColumn2Name(String column2Name) {
        this.column2Name = column2Name;
    }

    @DatabaseChangeProperty(description = "Name of the column to create", exampleValue = "full_name")
    public String getFinalColumnName() {
        return finalColumnName;
    }

    public void setFinalColumnName(String finalColumnName) {
        this.finalColumnName = finalColumnName;
    }

    @DatabaseChangeProperty(description = "Data type of the column to create", exampleValue = "varchar(255)")
    public String getFinalColumnType() {
        return finalColumnType;
    }

    public void setFinalColumnType(String finalColumnType) {
        this.finalColumnType = finalColumnType;
    }

    @Override
    public boolean generateStatementsVolatile(ExecutionEnvironment env) {
        if (env.getTargetDatabase() instanceof SQLiteDatabase) {
            return true;
        }
        return false;
    }

    @Override
    public Statement[] generateStatements(ExecutionEnvironment env) {
        Database database = env.getTargetDatabase();

        List<Statement> statements = new ArrayList<Statement>();

        AddColumnChange addNewColumnChange = new AddColumnChange();
        addNewColumnChange.setSchemaName(schemaName);
        addNewColumnChange.setTableName(getTableName());
        AddColumnConfig columnConfig = new AddColumnConfig();
        columnConfig.setName(getFinalColumnName());
        columnConfig.setType(getFinalColumnType());
        addNewColumnChange.addColumn(columnConfig);
        statements.addAll(Arrays.asList(addNewColumnChange.generateStatements(env)));

        String updateStatement = "UPDATE " + database.escapeTableName(getCatalogName(), getSchemaName(), getTableName()) +
                " SET " + database.escapeObjectName(getFinalColumnName(), Column.class)
                + " = " + database.getConcatSql(database.escapeObjectName(getColumn1Name(), Column.class)
                , "'" + getJoinString() + "'", database.escapeObjectName(getColumn2Name(), Column.class));

        statements.add(new RawSqlStatement(updateStatement));
        
        if (database instanceof SQLiteDatabase) {
            // SQLite does not support this ALTER TABLE operation until now.
			// For more information see: http://www.sqlite.org/omitted.html
			// This is a small work around...
    		
			// define alter table logic
    		AlterTableVisitor rename_alter_visitor = new AlterTableVisitor() {
    			@Override
                public ColumnConfig[] getColumnsToAdd() {
    				ColumnConfig[] new_columns = new ColumnConfig[1];
    				ColumnConfig new_column = new ColumnConfig();
    		        new_column.setName(getFinalColumnName());
    		        new_column.setType(getFinalColumnType());
    				new_columns[0] = new_column;
    				return new_columns;
    			}
    			@Override
                public boolean copyThisColumn(ColumnConfig column) {
    				return !(column.getName().equals(getColumn1Name()) ||
    						column.getName().equals(getColumn2Name()));
    			}
    			@Override
                public boolean createThisColumn(ColumnConfig column) {
    				return !(column.getName().equals(getColumn1Name()) ||
    						column.getName().equals(getColumn2Name()));
    			}
    			@Override
                public boolean createThisIndex(Index index) {
    				return !(index.getColumns().contains(getColumn1Name()) ||
    						index.getColumns().contains(getColumn2Name()));
    			}
    		};
        	
        	try {
        		// alter table
				statements.addAll(SQLiteDatabase.getAlterTableStatements(
						rename_alter_visitor,
                        env,getCatalogName(), getSchemaName(),getTableName()));
    		} catch (Exception e) {
				e.printStackTrace();
			}
    		
        } else {
        	// ...if it is not a SQLite database 
        	
	        DropColumnChange dropColumn1Change = new DropColumnChange();
	        dropColumn1Change.setSchemaName(schemaName);
	        dropColumn1Change.setTableName(getTableName());
	        dropColumn1Change.setColumnName(getColumn1Name());
	        statements.addAll(Arrays.asList(dropColumn1Change.generateStatements(env)));
	
	        DropColumnChange dropColumn2Change = new DropColumnChange();
	        dropColumn2Change.setSchemaName(schemaName);
	        dropColumn2Change.setTableName(getTableName());
	        dropColumn2Change.setColumnName(getColumn2Name());
	        statements.addAll(Arrays.asList(dropColumn2Change.generateStatements(env)));
        
        }
        return statements.toArray(new Statement[statements.size()]);

    }

    @Override
    public String getConfirmationMessage() {
        return "Columns "+getTableName()+"."+getColumn1Name()+" and "+getTableName()+"."+getColumn2Name()+" merged";
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
}

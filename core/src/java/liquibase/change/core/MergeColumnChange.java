package liquibase.change.core;

import liquibase.change.AbstractChange;
import liquibase.change.ChangeMetaData;
import liquibase.change.ColumnConfig;
import liquibase.database.Database;
import liquibase.database.core.DerbyDatabase;
import liquibase.database.core.SQLiteDatabase;
import liquibase.database.core.SQLiteDatabase.AlterTableVisitor;
import liquibase.database.structure.Index;
import liquibase.exception.UnsupportedChangeException;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.RawSqlStatement;
import liquibase.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Combines data from two existing columns into a new column and drops the original columns.
 */
public class MergeColumnChange extends AbstractChange {

    private String schemaName;
    private String tableName;
    private String column1Name;
    private String joinString;
    private String column2Name;
    private String finalColumnName;
    private String finalColumnType;

    public MergeColumnChange() {
        super("mergeColumns", "Merge Column", ChangeMetaData.PRIORITY_DEFAULT);
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

    public String getColumn1Name() {
        return column1Name;
    }

    public void setColumn1Name(String column1Name) {
        this.column1Name = column1Name;
    }

    public String getJoinString() {
        return joinString;
    }

    public void setJoinString(String joinString) {
        this.joinString = joinString;
    }

    public String getColumn2Name() {
        return column2Name;
    }

    public void setColumn2Name(String column2Name) {
        this.column2Name = column2Name;
    }

    public String getFinalColumnName() {
        return finalColumnName;
    }

    public void setFinalColumnName(String finalColumnName) {
        this.finalColumnName = finalColumnName;
    }

    public String getFinalColumnType() {
        return finalColumnType;
    }

    public void setFinalColumnType(String finalColumnType) {
        this.finalColumnType = finalColumnType;
    }

    public SqlStatement[] generateStatements(Database database) {

        List<SqlStatement> statements = new ArrayList<SqlStatement>();

        AddColumnChange addNewColumnChange = new AddColumnChange();
        String schemaName = getSchemaName() == null?database.getDefaultSchemaName():getSchemaName();
        addNewColumnChange.setSchemaName(schemaName);
        addNewColumnChange.setTableName(getTableName());
        ColumnConfig columnConfig = new ColumnConfig();
        columnConfig.setName(getFinalColumnName());
        columnConfig.setType(getFinalColumnType());
        addNewColumnChange.addColumn(columnConfig);
        statements.addAll(Arrays.asList(addNewColumnChange.generateStatements(database)));

        String updateStatement = "UPDATE " + database.escapeTableName(schemaName, getTableName()) + " SET " + getFinalColumnName() + " = " + database.getConcatSql(getColumn1Name(), "'"+getJoinString()+"'", getColumn2Name());

        statements.add(new RawSqlStatement(updateStatement));
        
        if (database instanceof SQLiteDatabase) {
        	// SQLite does not support this ALTER TABLE operation until now.
			// For more information see: http://www.sqlite.org/omitted.html
			// This is a small work around...
    		
			// define alter table logic
    		AlterTableVisitor rename_alter_visitor = new AlterTableVisitor() {
    			public ColumnConfig[] getColumnsToAdd() {
    				ColumnConfig[] new_columns = new ColumnConfig[1];
    				ColumnConfig new_column = new ColumnConfig();
    		        new_column.setName(getFinalColumnName());
    		        new_column.setType(getFinalColumnType());
    				new_columns[0] = new ColumnConfig(new_column);
    				return new_columns;
    			}
    			public boolean copyThisColumn(ColumnConfig column) {
    				return !(column.getName().equals(getColumn1Name()) ||
    						column.getName().equals(getColumn2Name()));
    			}
    			public boolean createThisColumn(ColumnConfig column) {
    				return !(column.getName().equals(getColumn1Name()) ||
    						column.getName().equals(getColumn2Name()));
    			}
    			public boolean createThisIndex(Index index) {
    				return !(index.getColumns().contains(getColumn1Name()) ||
    						index.getColumns().contains(getColumn2Name()));
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
    		
        } else {
        	// ...if it is not a SQLite database 
        	
	        DropColumnChange dropColumn1Change = new DropColumnChange();
	        dropColumn1Change.setSchemaName(schemaName);
	        dropColumn1Change.setTableName(getTableName());
	        dropColumn1Change.setColumnName(getColumn1Name());
	        statements.addAll(Arrays.asList(dropColumn1Change.generateStatements(database)));
	
	        DropColumnChange dropColumn2Change = new DropColumnChange();
	        dropColumn2Change.setSchemaName(schemaName);
	        dropColumn2Change.setTableName(getTableName());
	        dropColumn2Change.setColumnName(getColumn2Name());
	        statements.addAll(Arrays.asList(dropColumn2Change.generateStatements(database)));
        
        }
        return statements.toArray(new SqlStatement[statements.size()]);

    }

    public SqlStatement[] generateStatements(@SuppressWarnings("unused") DerbyDatabase database) throws UnsupportedChangeException {
        throw new UnsupportedChangeException("Derby does not currently support merging columns");
    }

    public String getConfirmationMessage() {
        return "Columns "+getTableName()+"."+getColumn1Name()+" and "+getTableName()+"."+getColumn2Name()+" merged";
    }
}

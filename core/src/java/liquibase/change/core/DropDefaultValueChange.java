package liquibase.change.core;

import liquibase.database.Database;
import liquibase.database.SQLiteDatabase;
import liquibase.database.SQLiteDatabase.AlterTableVisitor;
import liquibase.database.structure.Index;
import liquibase.statement.DropDefaultValueStatement;
import liquibase.statement.SqlStatement;
import liquibase.util.StringUtils;
import liquibase.change.AbstractChange;
import liquibase.change.ChangeMetaData;
import liquibase.change.ColumnConfig;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Removes the default value from an existing column.
 */
public class DropDefaultValueChange extends AbstractChange {

    private String schemaName;
    private String tableName;
    private String columnName;
    private String columnDataType;

    public DropDefaultValueChange() {
        super("dropDefaultValue", "Drop Default Value", ChangeMetaData.PRIORITY_DEFAULT);
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

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }
    
    public String getColumnDataType() {
		return columnDataType;
	}
    
    public void setColumnDataType(String columnDataType) {
		this.columnDataType = columnDataType;
	}

    public SqlStatement[] generateStatements(Database database) {

//todo    	if (database instanceof SQLiteDatabase) {
//    		// return special statements for SQLite databases
//    		return generateStatementsForSQLiteDatabase(database);
//        }
    	
        return new SqlStatement[]{
                new DropDefaultValueStatement(getSchemaName() == null?database.getDefaultSchemaName():getSchemaName(), getTableName(), getColumnName(), getColumnDataType()),
        };
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
				if (column.getName().equals(getColumnName())) {
					column.setDefaultValue(null);
					column.setDefaultValueBoolean(null);
					column.setDefaultValueDate((Date)null);
					column.setDefaultValueNumeric((Number)null);
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
        return "Default value dropped from "+getTableName()+"."+getColumnName();
    }
}

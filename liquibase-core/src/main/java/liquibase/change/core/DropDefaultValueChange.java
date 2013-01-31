package liquibase.change.core;

import liquibase.change.*;
import liquibase.database.Database;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.DropDefaultValueStatement;
import liquibase.structure.core.Column;

/**
 * Removes the default value from an existing column.
 */
@DatabaseChange(name="dropDefaultValue", description="Drop Default Value", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "column")
public class DropDefaultValueChange extends AbstractChange {

    private String catalogName;
    private String schemaName;
    private String tableName;
    private String columnName;
    private String columnDataType;

    @DatabaseChangeProperty(mustEqualExisting ="column.relation.catalog")
    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    @DatabaseChangeProperty(mustEqualExisting ="column.relation.schema")
    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    @DatabaseChangeProperty(requiredForDatabase = "all", mustEqualExisting = "column.relation")
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @DatabaseChangeProperty(requiredForDatabase = "all", mustEqualExisting = "column")
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
                new DropDefaultValueStatement(getCatalogName(), getSchemaName(), getTableName(), getColumnName(), getColumnDataType()),
        };
    }
    
//    private SqlStatement[] generateStatementsForSQLiteDatabase(Database database) {
//
//    	// SQLite does not support this ALTER TABLE operation until now.
//		// For more information see: http://www.sqlite.org/omitted.html.
//		// This is a small work around...
//
//    	List<SqlStatement> statements = new ArrayList<SqlStatement>();
//
//		// define alter table logic
//		AlterTableVisitor rename_alter_visitor = new AlterTableVisitor() {
//			public ColumnConfig[] getColumnsToAdd() {
//				return new ColumnConfig[0];
//			}
//			public boolean copyThisColumn(ColumnConfig column) {
//				return true;
//			}
//			public boolean createThisColumn(ColumnConfig column) {
//				if (column.getName().equals(getColumnName())) {
//					column.setDefaultValue(null);
//					column.setDefaultValueBoolean(null);
//					column.setDefaultValueDate((Date)null);
//					column.setDefaultValueNumeric((Number)null);
//				}
//				return true;
//			}
//			public boolean createThisIndex(Index index) {
//				return true;
//			}
//		};
//
//    	try {
//    		// alter table
//			statements.addAll(SQLiteDatabase.getAlterTableStatements(
//					rename_alter_visitor,
//					database,getCatalogName(), getSchemaName(),getTableName()));
//    	} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//    	return statements.toArray(new SqlStatement[statements.size()]);
//    }

    public String getConfirmationMessage() {
        return "Default value dropped from "+getTableName()+"."+getColumnName();
    }
}

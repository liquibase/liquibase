package liquibase.change.core;

import liquibase.change.*;
import liquibase.database.Database;
import liquibase.database.core.DB2Database;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.DropColumnStatement;
import liquibase.statement.core.ReorganizeTableStatement;

import java.util.ArrayList;
import java.util.List;

/**
 * Drops an existing column from a table.
 */
@DatabaseChange(name="dropColumn", description = "Drop Column", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "column")
public class DropColumnChange extends AbstractChange {

    private String catalogName;
    private String schemaName;
    private String tableName;
    private String columnName;

    @DatabaseChangeProperty(requiredForDatabase = "all",mustApplyTo = "column")
    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }


    @DatabaseChangeProperty(mustApplyTo ="column.relation.schema.catalog")
    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    @DatabaseChangeProperty(mustApplyTo ="column.relation.schema")
    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    @DatabaseChangeProperty(requiredForDatabase = "all", mustApplyTo = "column.relation")
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public SqlStatement[] generateStatements(Database database) {
     
//todo        if (database instanceof SQLiteDatabase) {		
//        	// return special statements for SQLite databases
//    		return generateStatementsForSQLiteDatabase(database);
//		}
			
        List<SqlStatement> statements = new ArrayList<SqlStatement>();

        statements.add(new DropColumnStatement(getCatalogName(), getSchemaName(), getTableName(), getColumnName()));
        if (database instanceof DB2Database) {
            statements.add(new ReorganizeTableStatement(getCatalogName(), getSchemaName(), getTableName()));
        }
        
        return statements.toArray(new SqlStatement[statements.size()]);
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
//			public boolean createThisColumn(ColumnConfig column) {
//				return !column.getName().equals(getColumnName());
//			}
//			public boolean copyThisColumn(ColumnConfig column) {
//				return !column.getName().equals(getColumnName());
//			}
//			public boolean createThisIndex(Index index) {
//				return !index.getColumns().contains(getColumnName());
//			}
//		};
//
//    	try {
//    		// alter table
//			statements.addAll(SQLiteDatabase.getAlterTableStatements(
//					rename_alter_visitor,
//					database,getCatalogName(), getSchemaName(),getTableName()));
//
//		}  catch (Exception e) {
//			e.printStackTrace();
//		}
//
//		return statements.toArray(new SqlStatement[statements.size()]);
//    }

    public String getConfirmationMessage() {
        return "Column " + getTableName() + "." + getColumnName() + " dropped";
    }
}

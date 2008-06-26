package liquibase.change;

import liquibase.database.DB2Database;
import liquibase.database.Database;
import liquibase.database.SQLiteDatabase;
import liquibase.database.SQLiteDatabase.AlterTableVisitor;
import liquibase.database.sql.DropColumnStatement;
import liquibase.database.sql.ReorganizeTableStatement;
import liquibase.database.sql.SqlStatement;
import liquibase.database.structure.Column;
import liquibase.database.structure.DatabaseObject;
import liquibase.database.structure.Index;
import liquibase.database.structure.Table;
import liquibase.exception.JDBCException;
import liquibase.exception.UnsupportedChangeException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.*;

/**
 * Drops an existing column from a table.
 */
public class DropColumnChange extends AbstractChange {

    private String schemaName;
    private String tableName;
    private String columnName;

    public DropColumnChange() {
        super("dropColumn", "Drop Column");
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }


    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
        List<SqlStatement> statements = new ArrayList<SqlStatement>();
        String schemaName = getSchemaName() == null?database.getDefaultSchemaName():getSchemaName();

        if (database instanceof SQLiteDatabase) {		
			// SQLite does not support this ALTER TABLE operation until now.
			// For more information see: http://www.sqlite.org/omitted.html.
			// This is a small work around...
			
			// define alter table logic
    		AlterTableVisitor rename_alter_visitor = new AlterTableVisitor() {
    			public ColumnConfig[] getColumnsToAdd() {
    				return new ColumnConfig[0];
    			}
    			public boolean createThisColumn(ColumnConfig column) {
    				return !column.getName().equals(getColumnName());
    			}
    			public boolean copyThisColumn(ColumnConfig column) {
    				return !column.getName().equals(getColumnName());
    			}
    			public boolean createThisIndex(Index index) {
    				return !index.getColumns().contains(getColumnName());
    			}
    		};  
    		
        	try {
        		// alter table
				statements.addAll(SQLiteDatabase.getAlterTableStatements(
						rename_alter_visitor,
						database,getSchemaName(),getTableName()));
				
			}  catch (JDBCException e) {
				e.printStackTrace();
			}
		} else {
			
			// ...if it is not a SQLite database 
	        statements.add(new DropColumnStatement(schemaName, getTableName(), getColumnName()));
	        if (database instanceof DB2Database) {
	            statements.add(new ReorganizeTableStatement(schemaName, getTableName()));
	        }
	        
		}       
        return statements.toArray(new SqlStatement[statements.size()]);
    }

    public String getConfirmationMessage() {
        return "Column " + getTableName() + "." + getColumnName() + " dropped";
    }

    public Element createNode (Document currentChangeLogFileDOM) {
        Element element = currentChangeLogFileDOM.createElement("dropColumn");
        if (getSchemaName() != null) {
            element.setAttribute("schemaName", getSchemaName());
        }

        element.setAttribute("tableName", getTableName());
        element.setAttribute("columnName", getColumnName());
        return element;
    }

    public Set<DatabaseObject> getAffectedDatabaseObjects() {


        Table table = new Table(getTableName());

        Column column = new Column();
        column.setTable(table);
        column.setName(columnName);

        return new HashSet<DatabaseObject>(Arrays.asList(table, column));

    }

}

package liquibase.change;

import liquibase.database.Database;
import liquibase.database.SQLiteDatabase;
import liquibase.database.SQLiteDatabase.AlterTableVisitor;
import liquibase.database.sql.RenameColumnStatement;
import liquibase.database.sql.SqlStatement;
import liquibase.database.structure.Column;
import liquibase.database.structure.DatabaseObject;
import liquibase.database.structure.Index;
import liquibase.database.structure.Table;
import liquibase.exception.JDBCException;
import liquibase.exception.UnsupportedChangeException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Renames an existing column.
 */
public class RenameColumnChange extends AbstractChange {

    private String schemaName;
    private String tableName;
    private String oldColumnName;
    private String newColumnName;
    private String columnDataType;

    public RenameColumnChange() {
        super("renameColumn", "Rename Column");
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

    public String getOldColumnName() {
        return oldColumnName;
    }

    public void setOldColumnName(String oldColumnName) {
        this.oldColumnName = oldColumnName;
    }

    public String getNewColumnName() {
        return newColumnName;
    }

    public void setNewColumnName(String newColumnName) {
        this.newColumnName = newColumnName;
    }

    public String getColumnDataType() {
        return columnDataType;
    }

    public void setColumnDataType(String columnDataType) {
        this.columnDataType = columnDataType;
    }

    public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
    	List<SqlStatement> statements = new ArrayList<SqlStatement>();
    	
    	if (database instanceof SQLiteDatabase) {
    		// SQLite does not support this ALTER TABLE operation until now.
			// For more information see: http://www.sqlite.org/omitted.html.
			// This is a small work around...
        	try {
        		// define alter table logic
        		AlterTableVisitor rename_alter_visitor = 
        		new AlterTableVisitor() {
        			public ColumnConfig[] getColumnsToAdd() {
        				return new ColumnConfig[0];
        			}
        			public boolean copyThisColumn(ColumnConfig column) {
        				return true;
        			}
        			public boolean createThisColumn(ColumnConfig column) {
        				if (column.getName().equals(getOldColumnName())) {
        					column.setName(getNewColumnName());
        				}
        				return true;
        			}
        			public boolean createThisIndex(Index index) {
        				if (index.getColumns().contains(getOldColumnName())) {
    						index.getColumns().remove(getOldColumnName());
    						index.getColumns().add(getNewColumnName());
    					}
        				return true;
        			}
        		};
        		// alter table
				statements.addAll(SQLiteDatabase.getAlterTableStatements(
						rename_alter_visitor,
						database,getSchemaName(),getTableName()));
			} catch (JDBCException e) {
				System.err.println(e);
				e.printStackTrace();
			}
        } else {
	    	statements.add(new RenameColumnStatement(
	    			getSchemaName()==null?database.getDefaultSchemaName():getSchemaName(), 
	    			getTableName(), getOldColumnName(), getNewColumnName(), 
	    			getColumnDataType()));
        }
    	return statements.toArray(new SqlStatement[statements.size()]);
    }

    protected Change[] createInverses() {
        RenameColumnChange inverse = new RenameColumnChange();
        inverse.setSchemaName(getSchemaName());
        inverse.setTableName(getTableName());
        inverse.setOldColumnName(getNewColumnName());
        inverse.setNewColumnName(getOldColumnName());
        inverse.setColumnDataType(getColumnDataType());

        return new Change[]{
                inverse
        };
    }

    public String getConfirmationMessage() {
        return "Column "+tableName+"."+ oldColumnName + " renamed to " + newColumnName;
    }

    public Element createNode(Document currentChangeLogFileDOM) {
        Element node = currentChangeLogFileDOM.createElement("renameColumn");
        if (getSchemaName() != null) {
            node.setAttribute("schemaName", getSchemaName());
        }
        
        node.setAttribute("tableName", getTableName());
        node.setAttribute("oldColumnName", getOldColumnName());
        node.setAttribute("newColumnName", getNewColumnName());

        return node;
    }

    public Set<DatabaseObject> getAffectedDatabaseObjects() {

        Table table = new Table(getTableName());

        Column oldColumn = new Column();
        oldColumn.setTable(table);
        oldColumn.setName(getOldColumnName());

        Column newColumn = new Column();
        newColumn.setTable(table);
        newColumn.setName(getNewColumnName());

        return new HashSet<DatabaseObject>(Arrays.asList(table, oldColumn, newColumn));

    }

}

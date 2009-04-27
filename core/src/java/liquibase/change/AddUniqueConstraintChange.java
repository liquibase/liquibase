package liquibase.change;

import liquibase.database.Database;
import liquibase.database.SQLiteDatabase;
import liquibase.database.SQLiteDatabase.AlterTableVisitor;
import liquibase.database.statement.AddUniqueConstraintStatement;
import liquibase.database.statement.SqlStatement;
import liquibase.database.structure.Column;
import liquibase.database.structure.DatabaseObject;
import liquibase.database.structure.Index;
import liquibase.database.structure.Table;
import liquibase.exception.JDBCException;
import liquibase.exception.UnsupportedChangeException;
import liquibase.exception.InvalidChangeDefinitionException;
import liquibase.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        super("addUniqueConstraint", "Add Unique Constraint");
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

    public void validate(Database database) throws InvalidChangeDefinitionException {
        if (StringUtils.trimToNull(tableName) == null) {
            throw new InvalidChangeDefinitionException("tableName is required", this);
        }
        if (StringUtils.trimToNull(columnNames) == null) {
            throw new InvalidChangeDefinitionException("columnNames is required", this);
        }

    }

    public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
        
    	if (database instanceof SQLiteDatabase) {
    		// return special statements for SQLite databases
    		return generateStatementsForSQLiteDatabase(database);
        }
    	
    	AddUniqueConstraintStatement statement = new AddUniqueConstraintStatement(getSchemaName() == null?database.getDefaultSchemaName():getSchemaName(), getTableName(), getColumnNames(), getConstraintName());
        statement.setTablespace(getTablespace());

        return new SqlStatement[] { statement };
    }
    
    private SqlStatement[] generateStatementsForSQLiteDatabase(Database database) 
			throws UnsupportedChangeException {
    	
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
    	} catch (JDBCException e) {
			e.printStackTrace();
		}
    	
    	return statements.toArray(new SqlStatement[statements.size()]);
    }

    public String getConfirmationMessage() {
        return "Unique constraint added to "+getTableName()+"("+getColumnNames()+")";
    }

    protected Change[] createInverses() {
        DropUniqueConstraintChange inverse = new DropUniqueConstraintChange();
        inverse.setSchemaName(getSchemaName());
        inverse.setTableName(getTableName());
        inverse.setConstraintName(getConstraintName());

        return new Change[]{
                inverse,
        };
    }

    public Element createNode(Document currentChangeLogFileDOM) {
        Element element = currentChangeLogFileDOM.createElement(getChangeName());
        if (getSchemaName() != null) {
            element.setAttribute("schemaName", getSchemaName());
        }
        if (getTablespace() != null) {
            element.setAttribute("tablespace", getTablespace());            
        }

        element.setAttribute("tableName", getTableName());
        element.setAttribute("columnNames", getColumnNames());
        element.setAttribute("constraintName", getConstraintName());

        return element;
    }

    public Set<DatabaseObject> getAffectedDatabaseObjects() {

        Set<DatabaseObject> returnSet = new HashSet<DatabaseObject>();

        Table table = new Table(getTableName());
        returnSet.add(table);

        for (String columnName : getColumnNames().split(",")) {
            Column column = new Column();
            column.setTable(table);
            column.setName(columnName.trim());

            returnSet.add(column);
        }

        return returnSet;

    }

}

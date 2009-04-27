package liquibase.change;

import liquibase.database.Database;
import liquibase.database.SQLiteDatabase;
import liquibase.database.SQLiteDatabase.AlterTableVisitor;
import liquibase.database.statement.DropPrimaryKeyStatement;
import liquibase.database.statement.SqlStatement;
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
 * Removes an existing primary key.
 */
public class DropPrimaryKeyChange extends AbstractChange {
    private String schemaName;
    private String tableName;
    private String constraintName;

    public DropPrimaryKeyChange() {
        super("dropPrimaryKey", "Drop Primary Key");
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

    public String getConstraintName() {
        return constraintName;
    }

    public void setConstraintName(String constraintName) {
        this.constraintName = constraintName;
    }

    public void validate(Database database) throws InvalidChangeDefinitionException {
    }

    public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
    	
    	if (database instanceof SQLiteDatabase) {
    		// return special statements for SQLite databases
    		return generateStatementsForSQLiteDatabase(database);
        }
    	
        return new SqlStatement[]{
                new DropPrimaryKeyStatement(getSchemaName() == null?database.getDefaultSchemaName():getSchemaName(), getTableName(), getConstraintName()),
        };
    }
    
    private SqlStatement[] generateStatementsForSQLiteDatabase(Database database) 
			throws UnsupportedChangeException {
    	
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
					column.getConstraints().setPrimaryKey(false);
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
        return "Primary key dropped from "+getTableName();
    }

    public Element createNode(Document currentChangeLogFileDOM) {
        Element node = currentChangeLogFileDOM.createElement(getChangeName());
        if (getSchemaName() != null) {
            node.setAttribute("schemaName", getSchemaName());
        }
        
        node.setAttribute("tableName", getTableName());
        if (getConstraintName() != null) {
            node.setAttribute("constraintName", getConstraintName());
        }
        return node;
    }

    public Set<DatabaseObject> getAffectedDatabaseObjects() {

        Set<DatabaseObject> dbObjects = new HashSet<DatabaseObject>();

        Table table = new Table(getTableName());
        dbObjects.add(table);

        return dbObjects;

    }

}

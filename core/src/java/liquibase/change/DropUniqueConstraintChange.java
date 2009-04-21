package liquibase.change;

import liquibase.database.Database;
import liquibase.database.SQLiteDatabase;
import liquibase.database.SybaseASADatabase;
import liquibase.database.SQLiteDatabase.AlterTableVisitor;
import liquibase.database.statement.DropUniqueConstraintStatement;
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
 * Removes an existing unique constraint.
 */
public class DropUniqueConstraintChange extends AbstractChange {
    private String schemaName;
    private String tableName;
    private String constraintName;
    /**
     * Sybase ASA does drop unique constraint not by name, but using list of the columns in unique clause.
     */
    private String uniqueColumns;

	public DropUniqueConstraintChange() {
        super("dropUniqueConstraint", "Drop Unique Constraint");
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

    public String getUniqueColumns() {
		return uniqueColumns;
	}

	public void setUniqueColumns(String uniqueColumns) {
		this.uniqueColumns = uniqueColumns;
	}

    public void validate(Database database) throws InvalidChangeDefinitionException {
    	if (database instanceof SybaseASADatabase) {
            if (StringUtils.trimToNull(constraintName) == null && StringUtils.trimToNull(constraintName) == null) {
                throw new InvalidChangeDefinitionException("either constraintName or uniqueNames is required", this);
            }
    	} else if (StringUtils.trimToNull(constraintName) == null) {
            throw new InvalidChangeDefinitionException("constraintName is required", this);
        }

    }

    public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
        
    	if (database instanceof SQLiteDatabase) {
    		// return special statements for SQLite databases
    		return generateStatementsForSQLiteDatabase(database);
        }
    	DropUniqueConstraintStatement statement = new DropUniqueConstraintStatement(getSchemaName() == null?database.getDefaultSchemaName():getSchemaName(), getTableName(), getConstraintName());
    	if (database instanceof SybaseASADatabase) {
    		statement.setUniqueColumns(uniqueColumns);
    	}
    	return new SqlStatement[]{
			statement
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
    				column.getConstraints().setUnique(false);            					
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
        return "Unique constraint "+getConstraintName()+" dropped from "+getTableName();
    }

    public Element createNode(Document currentChangeLogFileDOM) {
        Element node = currentChangeLogFileDOM.createElement(getTagName());
        if (getSchemaName() != null) {
            node.setAttribute("schemaName", getSchemaName());
        }
        
        node.setAttribute("tableName", getTableName());
        node.setAttribute("constraintName", constraintName);
        return node;
    }

    public Set<DatabaseObject> getAffectedDatabaseObjects() {

        Set<DatabaseObject> returnSet = new HashSet<DatabaseObject>();

        Table table = new Table(getTableName());
        returnSet.add(table);

        return returnSet;

    }
}

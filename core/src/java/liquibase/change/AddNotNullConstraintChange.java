package liquibase.change;

import liquibase.database.*;
import liquibase.database.sql.SqlStatement;
import liquibase.database.sql.RawSqlStatement;
import liquibase.database.structure.Column;
import liquibase.database.structure.DatabaseObject;
import liquibase.database.structure.Table;
import liquibase.exception.UnsupportedChangeException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.*;

/**
 * Adds a not-null constraint to an existing column.
 */
public class AddNotNullConstraintChange extends AbstractChange {
    private String tableName;
    private String columnName;
    private String defaultNullValue;
    private String columnDataType;


    public AddNotNullConstraintChange() {
        super("addNotNullConstraint", "Add Not-Null Constraint");
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

    public String getDefaultNullValue() {
        return defaultNullValue;
    }

    public void setDefaultNullValue(String defaultNullValue) {
        this.defaultNullValue = defaultNullValue;
    }

    public String getColumnDataType() {
        return columnDataType;
    }

    public void setColumnDataType(String columnDataType) {
        this.columnDataType = columnDataType;
    }


    private SqlStatement generateUpdateStatement() {
        return new RawSqlStatement("UPDATE " + tableName + " SET " + columnName + "='" + defaultNullValue + "' WHERE " + columnName + " IS NULL");
    }

    public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
        if (database instanceof SybaseDatabase) {
            return generateSybaseStatements();
        } else if (database instanceof MSSQLDatabase) {
            return generateMSSQLStatements();
        } else if (database instanceof MySQLDatabase) {
            return generateMySQLStatements();
        } else if (database instanceof OracleDatabase) {
            return generateOracleStatements();
        } else if (database instanceof DerbyDatabase) {
            return generateDerbyStatements();
        } else if (database instanceof H2Database) {
            return generateH2Statements();
        } else if (database instanceof CacheDatabase) {
        	return generateCacheStatements();
        }

        List<SqlStatement> statements = new ArrayList<SqlStatement>();
        if (defaultNullValue != null) {
            statements.add(generateUpdateStatement());
        }

        statements.add(new RawSqlStatement("ALTER TABLE " + getTableName() + " ALTER COLUMN  " + getColumnName() + " SET NOT NULL "));

        if (database instanceof DB2Database) {
            statements.add(new RawSqlStatement("CALL SYSPROC.ADMIN_CMD ('REORG TABLE "+getTableName()+"')"));
        }

        return statements.toArray(new SqlStatement[statements.size()]);

    }

    private SqlStatement[] generateCacheStatements() {
    	List<SqlStatement> statements = new ArrayList<SqlStatement>();
    	if (defaultNullValue != null) {
    		statements.add(generateUpdateStatement());
    	}
    	statements.add(new RawSqlStatement("COMMIT"));
    	statements.add(new RawSqlStatement("ALTER TABLE " + getTableName() + " ALTER COLUMN " + getColumnName() + " NOT NULL"));
    	
    	return statements.toArray(new SqlStatement[statements.size()]);
    }
    
    private SqlStatement[] generateSybaseStatements() {
        List<SqlStatement> statements = new ArrayList<SqlStatement>();
        if (defaultNullValue != null) {
            statements.add(generateUpdateStatement());
        }
        statements.add(new RawSqlStatement("COMMIT"));
        statements.add(new RawSqlStatement("ALTER TABLE " + getTableName() + " MODIFY " + getColumnName() + " NOT NULL"));

        return statements.toArray(new SqlStatement[statements.size()]);
    }

    private SqlStatement[] generateMSSQLStatements() {
        if (columnDataType == null) {
            throw new RuntimeException("columnDataType is required to add not null constraints with MS-SQL");
        }

        List<SqlStatement> statements = new ArrayList<SqlStatement>();
        if (defaultNullValue != null) {
            statements.add(generateUpdateStatement());
        }
        statements.add(new RawSqlStatement("ALTER TABLE " + getTableName() + " ALTER COLUMN " + getColumnName() + " " + columnDataType + " " + " NOT NULL"));

        return statements.toArray(new SqlStatement[statements.size()]);
    }

    private SqlStatement[] generateMySQLStatements() {
        if (columnDataType == null) {
            throw new RuntimeException("columnDataType is required to add not null constraints with MySQL");
        }

        List<SqlStatement> statements = new ArrayList<SqlStatement>();
        if (defaultNullValue != null) {
            statements.add(generateUpdateStatement());
        }
        statements.add(new RawSqlStatement("ALTER TABLE " + getTableName() + " MODIFY " + getColumnName() + " " + columnDataType + " NOT NULL"));

        return statements.toArray(new SqlStatement[statements.size()]);
    }

    private SqlStatement[] generateOracleStatements() {
        List<SqlStatement> statements = new ArrayList<SqlStatement>();
        if (defaultNullValue != null) {
            statements.add(generateUpdateStatement());
        }
        statements.add(new RawSqlStatement("ALTER TABLE " + getTableName() + " MODIFY " + getColumnName() + " NOT NULL"));

        return statements.toArray(new SqlStatement[statements.size()]);
    }

    public SqlStatement[] generateDerbyStatements()  {
        List<SqlStatement> statements = new ArrayList<SqlStatement>();
        if (defaultNullValue != null) {
            statements.add(generateUpdateStatement());
        }
        statements.add(new RawSqlStatement("ALTER TABLE " + getTableName() + " ALTER COLUMN  " + getColumnName() + " NOT NULL"));

        return statements.toArray(new SqlStatement[statements.size()]);
    }

    public SqlStatement[] generateH2Statements()  {
        List<SqlStatement> statements = new ArrayList<SqlStatement>();
        if (defaultNullValue != null) {
            statements.add(generateUpdateStatement());
        }
        statements.add(new RawSqlStatement("ALTER TABLE " + getTableName() + " ALTER COLUMN  " + getColumnName() + " "+getColumnDataType()+" NOT NULL"));

        return statements.toArray(new SqlStatement[statements.size()]);
    }

    protected Change[] createInverses() {
        DropNotNullConstraintChange inverse = new DropNotNullConstraintChange();
        inverse.setColumnName(getColumnName());
        inverse.setTableName(getTableName());
        inverse.setColumnDataType(getColumnDataType());

        return new Change[]{
                inverse
        };
    }

    public String getConfirmationMessage() {
        return "Null Constraint has been added to the column " + getColumnName() + " of the table " + getTableName();
    }


    public Element createNode(Document currentChangeLogFileDOM) {
        Element element = currentChangeLogFileDOM.createElement("addNotNullConstraint");
        element.setAttribute("tableName", getTableName());
        element.setAttribute("columnName", getColumnName());
        element.setAttribute("defaultNullValue", getDefaultNullValue());
        return element;
    }

    public Set<DatabaseObject> getAffectedDatabaseObjects() {

        Table table = new Table();
        table.setName(tableName);

        Column column = new Column();
        column.setTable(table);
        column.setName(columnName);



        return new HashSet<DatabaseObject>(Arrays.asList(table, column));

    }
}

package liquibase.migrator.change;

import liquibase.database.*;
import liquibase.migrator.exception.UnsupportedChangeException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

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


    private String generateUpdateStatement() {
        return "UPDATE " + tableName + " SET " + columnName + "='" + defaultNullValue + "' WHERE " + columnName + " IS NULL";
    }

    public String[] generateStatements(Database database) throws UnsupportedChangeException {
        if (database instanceof MSSQLDatabase) {
            return generateMSSQLStatements();
        } else if (database instanceof MySQLDatabase) {
            return generateMySQLStatements();
        } else if (database instanceof OracleDatabase) {
            return generateOracleStatements();
        } else if (database instanceof DerbyDatabase) {
            return generateDerbyStatements();
        }

        List<String> statements = new ArrayList<String>();
        if (defaultNullValue != null) {
            statements.add(generateUpdateStatement());
        }
        statements.add("ALTER TABLE " + getTableName() + " ALTER COLUMN  " + getColumnName() + " SET NOT NULL");

        if (database instanceof DB2Database) {
            statements.add("CALL SYSPROC.ADMIN_CMD ('REORG TABLE "+getTableName()+"')");
        }

        return statements.toArray(new String[statements.size()]);

    }

    private String[] generateMSSQLStatements() {
        if (columnDataType == null) {
            throw new RuntimeException("columnDataType is required to add not null constraints with MS-SQL");
        }

        List<String> statements = new ArrayList<String>();
        if (defaultNullValue != null) {
            statements.add(generateUpdateStatement());
        }
        statements.add("ALTER TABLE " + getTableName() + " ALTER COLUMN " + getColumnName() + " " + columnDataType + " " + " NOT NULL");

        return statements.toArray(new String[statements.size()]);
    }

    private String[] generateMySQLStatements() {
        if (columnDataType == null) {
            throw new RuntimeException("columnDataType is required to add not null constraints with MySQL");
        }

        List<String> statements = new ArrayList<String>();
        if (defaultNullValue != null) {
            statements.add(generateUpdateStatement());
        }
        statements.add("ALTER TABLE " + getTableName() + " MODIFY " + getColumnName() + " " + columnDataType + " NOT NULL");

        return statements.toArray(new String[statements.size()]);
    }

    private String[] generateOracleStatements() {
        List<String> statements = new ArrayList<String>();
        if (defaultNullValue != null) {
            statements.add(generateUpdateStatement());
        }
        statements.add("ALTER TABLE " + getTableName() + " MODIFY " + getColumnName() + " NOT NULL");

        return statements.toArray(new String[statements.size()]);
    }

    public String[] generateDerbyStatements() throws UnsupportedChangeException {
        List<String> statements = new ArrayList<String>();
        if (defaultNullValue != null) {
            statements.add(generateUpdateStatement());
        }
        statements.add("ALTER TABLE " + getTableName() + " ALTER COLUMN  " + getColumnName() + " NOT NULL");

        return statements.toArray(new String[statements.size()]);
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
}

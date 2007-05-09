package liquibase.migrator.change;

import liquibase.database.MSSQLDatabase;
import liquibase.database.MySQLDatabase;
import liquibase.database.OracleDatabase;
import liquibase.database.PostgresDatabase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;
import java.util.ArrayList;

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

    public String[] generateStatements(MSSQLDatabase database) {
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

    public String[] generateStatements(MySQLDatabase database) {
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

    public String[] generateStatements(OracleDatabase database) {
        List<String> statements = new ArrayList<String>();
        if (defaultNullValue != null) {
            statements.add(generateUpdateStatement());
        }
        statements.add("ALTER TABLE " + getTableName() + " MODIFY " + getColumnName() + " NOT NULL");

        return statements.toArray(new String[statements.size()]);
    }

    public String[] generateStatements(PostgresDatabase database) {
        List<String> statements = new ArrayList<String>();
        if (defaultNullValue != null) {
            statements.add(generateUpdateStatement());
        }
        statements.add("ALTER TABLE " + getTableName() + " ALTER COLUMN  " + getColumnName() + " SET NOT NULL");

        return statements.toArray(new String[statements.size()]);
    }

    protected AbstractChange[] createInverses() {
        DropNotNullConstraintChange inverse = new DropNotNullConstraintChange();
        inverse.setColumnName(getColumnName());
        inverse.setTableName(getTableName());
        inverse.setColumnDataType(getColumnDataType());

        return new AbstractChange[] {
                inverse
        };
    }

    public String getConfirmationMessage() {
        return "Null Constraint has been added to the column " + getColumnName() + " of the table " + getTableName();
    }


    public Element createNode(Document currentMigrationFileDOM) {
        Element element = currentMigrationFileDOM.createElement("addNotNullConstraint");
        element.setAttribute("tableName", getTableName());
        element.setAttribute("columnName", getColumnName());
        element.setAttribute("defaultNullValue", getDefaultNullValue());
        return element;
    }

    public void doRefactoring() {
        //To change body of created methods use File | Settings | File Templates.
    }
}

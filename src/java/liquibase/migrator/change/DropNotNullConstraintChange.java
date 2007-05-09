package liquibase.migrator.change;

import liquibase.database.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DropNotNullConstraintChange extends AbstractChange {
    private String tableName;
    private String columnName;
    private String columnDataType;


    public DropNotNullConstraintChange() {
        super("dropNotNullConstraint", "Drop Not-Null Constraint");
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

    public String getColumnDataType() {
        return columnDataType;
    }

    public void setColumnDataType(String columnDataType) {
        this.columnDataType = columnDataType;
    }

    public String[] generateStatements(MSSQLDatabase database) {
        if (columnDataType == null) {
            throw new RuntimeException("columnDataType is required to drop not null constraints with MS-SQL");
        }

        return new String[] { "ALTER TABLE " + tableName + " ALTER COLUMN " + columnName + " " + columnDataType + " NULL" };
    }

    public String[] generateStatements(OracleDatabase database) {
        return new String[] { "ALTER TABLE " + tableName + " MODIFY " + columnName + " NULL" };
    }

    public String[] generateStatements(MySQLDatabase database) {
        if (columnDataType == null) {
            throw new RuntimeException("columnDataType is required to drop not null constraints with MySQL");
        }

        return new String[] { "ALTER TABLE " + tableName + " MODIFY " + columnName + " " + columnDataType + " DEFAULT NULL" };
    }

    public String[] generateStatements(PostgresDatabase database) {
        return new String[] { "ALTER TABLE " + tableName + " ALTER COLUMN " + columnName + " DROP NOT NULL" };
    }

    protected AbstractChange[] createInverses() {
        AddNotNullConstraintChange inverse = new AddNotNullConstraintChange();
        inverse.setColumnName(getColumnName());
        inverse.setTableName(getTableName());
        inverse.setColumnDataType(getColumnDataType());

        return new AbstractChange[] {
                inverse
        };
    }

    public String getConfirmationMessage() {
        return "Null Constraint has been dropped to the column " + getColumnName() + " of the table " + getTableName();
    }

    public Element createNode(Document currentMigrationFileDOM) {
        Element element = currentMigrationFileDOM.createElement("dropNotNullConstraint");
        element.setAttribute("tableName", getTableName());
        element.setAttribute("columnName", getColumnName());
        return element;
    }
}

package liquibase.migrator.change;

import liquibase.database.*;
import liquibase.migrator.exception.UnsupportedChangeException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Drops a not-null constraint from an existing column.
 */
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

    public String[] generateStatements(Database database) throws UnsupportedChangeException {
        if (database instanceof SybaseDatabase) {
            return generateSybaseStatements();
        } else if (database instanceof MSSQLDatabase) {
            return generateMSSQLStatements();
        } else if (database instanceof MySQLDatabase) {
            return generateMySQLStatements();
        } else if (database instanceof OracleDatabase) {
            return generateOracleStatements();
        } else if (database instanceof DerbyDatabase) {
            return new String[]{"ALTER TABLE " + tableName + " ALTER COLUMN " + columnName + " NULL"};
        } else if (database instanceof HsqlDatabase) {
            return new String[]{"ALTER TABLE " + tableName + " ALTER COLUMN " + columnName + " NULL"};
        }

        return new String[]{"ALTER TABLE " + tableName + " ALTER COLUMN " + columnName + " DROP NOT NULL"};
    }

    private String[] generateSybaseStatements() {
        return new String[]{"ALTER TABLE " + tableName + " MODIFY " + columnName + " NULL"};
    }

    private String[] generateMSSQLStatements() {
        if (columnDataType == null) {
            throw new RuntimeException("columnDataType is required to drop not null constraints with MS-SQL");
        }

        return new String[]{"ALTER TABLE " + tableName + " ALTER COLUMN " + columnName + " " + columnDataType + " NULL"};
    }

    private String[] generateOracleStatements() {
        return new String[]{"ALTER TABLE " + tableName + " MODIFY " + columnName + " NULL"};
    }

    private String[] generateMySQLStatements() {
        if (columnDataType == null) {
            throw new RuntimeException("columnDataType is required to drop not null constraints with MySQL");
        }

        return new String[]{"ALTER TABLE " + tableName + " MODIFY " + columnName + " " + columnDataType + " DEFAULT NULL"};
    }

    protected Change[] createInverses() {
        AddNotNullConstraintChange inverse = new AddNotNullConstraintChange();
        inverse.setColumnName(getColumnName());
        inverse.setTableName(getTableName());
        inverse.setColumnDataType(getColumnDataType());

        return new Change[]{
                inverse
        };
    }

    public String getConfirmationMessage() {
        return "Null Constraint has been dropped to the column " + getColumnName() + " of the table " + getTableName();
    }

    public Element createNode(Document currentChangeLogFileDOM) {
        Element element = currentChangeLogFileDOM.createElement("dropNotNullConstraint");
        element.setAttribute("tableName", getTableName());
        element.setAttribute("columnName", getColumnName());
        return element;
    }
}

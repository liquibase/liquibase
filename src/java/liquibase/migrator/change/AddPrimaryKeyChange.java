package liquibase.migrator.change;

import liquibase.database.AbstractDatabase;
import liquibase.migrator.UnsupportedChangeException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Creates a primary key out of an existing column or set of columns.
 */
public class AddPrimaryKeyChange extends AbstractChange {

    private String tableName;
    private String columnNames;
    private String constraintName;

    public AddPrimaryKeyChange() {
        super("addPrimaryKey", "Add Primary Key");
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

    public String[] generateStatements(AbstractDatabase database) throws UnsupportedChangeException {
        if (getConstraintName() == null) {
            return new String[]{
                    "ALTER TABLE " + getTableName() + " ADD PRIMARY KEY (" + getColumnNames() + ")",
            };
        } else {
            return new String[]{
                    "ALTER TABLE " + getTableName() + " ADD CONSTRAINT " + getConstraintName() + " PRIMARY KEY (" + getColumnNames() + ")",
            };
        }
    }

    protected AbstractChange[] createInverses() {
        DropPrimaryKeyChange inverse = new DropPrimaryKeyChange();
        inverse.setTableName(getTableName());
        inverse.setConstraintName(getConstraintName());

        return new AbstractChange[]{
                inverse,
        };
    }

    public String getConfirmationMessage() {
        return "Primary Key Added";
    }

    public Element createNode(Document currentChangeLogFileDOM) {
        Element node = currentChangeLogFileDOM.createElement(getTagName());
        node.setAttribute("tableName", getTableName());
        node.setAttribute("columnNames", getColumnNames());
        if (getConstraintName() == null) {
            node.setAttribute("constraintName", getConstraintName());
        }

        return node;
    }
}

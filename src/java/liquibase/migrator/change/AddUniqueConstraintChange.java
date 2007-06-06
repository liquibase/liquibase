package liquibase.migrator.change;

import liquibase.database.AbstractDatabase;
import liquibase.migrator.UnsupportedChangeException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Adds a unique constraint to an existing column.
 */
public class AddUniqueConstraintChange extends AbstractChange {

    private String tableName;
    private String columnNames;
    private String constraintName;

    public AddUniqueConstraintChange() {
        super("addUniqueConstraint", "Add Unique Constraint");
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
        return new String[]{
                "ALTER TABLE " + getTableName() + " ADD CONSTRAINT " + getConstraintName() + " UNIQUE (" + getColumnNames() + ")"
        };
    }

    public String getConfirmationMessage() {
        return "Unique Constraint Added";
    }

    protected AbstractChange[] createInverses() {
        DropUniqueConstraintChange inverse = new DropUniqueConstraintChange();
        inverse.setTableName(getTableName());
        inverse.setConstraintName(getConstraintName());

        return new AbstractChange[]{
                inverse,
        };
    }

    public Element createNode(Document currentChangeLogFileDOM) {
        Element element = currentChangeLogFileDOM.createElement(getTagName());
        element.setAttribute("tableName", getTableName());
        element.setAttribute("columnNames", getColumnNames());
        element.setAttribute("constraintName", getConstraintName());

        return element;
    }
}

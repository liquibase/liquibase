package liquibase.migrator.change;

import liquibase.database.AbstractDatabase;
import liquibase.database.MySQLDatabase;
import liquibase.migrator.UnsupportedChangeException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Removes an existing unique constraint.
 */
public class DropUniqueConstraintChange extends AbstractChange {
    private String tableName;
    private String constraintName;

    public DropUniqueConstraintChange() {
        super("dropUniqueConstraint", "Drop Unique Constraint");
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

    public String[] generateStatements(AbstractDatabase database) throws UnsupportedChangeException {
        if (database instanceof MySQLDatabase) {
            return new String[]{ "ALTER TABLE " + getTableName() + " DROP KEY " + getConstraintName(), };
        }

        return new String[]{
                "ALTER TABLE " + getTableName() + " DROP CONSTRAINT " + getConstraintName(),
        };
    }

    public String getConfirmationMessage() {
        return "Unique Constraint Key Dropped";
    }

    public Element createNode(Document currentChangeLogFileDOM) {
        Element node = currentChangeLogFileDOM.createElement(getTagName());
        node.setAttribute("tableName", getTableName());
        return node;
    }

}

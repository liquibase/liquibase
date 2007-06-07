package liquibase.migrator.change;

import liquibase.database.Database;
import liquibase.migrator.UnsupportedChangeException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Adds a foreign key constraint to an existing column.
 */
public class AddForeignKeyConstraintChange extends AbstractChange {
    private String baseTableName;
    private String baseColumnNames;

    private String referencedTableName;
    private String referencedColumnNames;

    private String constraintName;

    private Boolean deferrable;
    private Boolean initiallyDeferred;

    public AddForeignKeyConstraintChange() {
        super("addForeignKeyConstraint", "Add Foreign Key Constraint");
    }

    public String getBaseTableName() {
        return baseTableName;
    }

    public void setBaseTableName(String baseTableName) {
        this.baseTableName = baseTableName;
    }

    public String getBaseColumnNames() {
        return baseColumnNames;
    }

    public void setBaseColumnNames(String baseColumnNames) {
        this.baseColumnNames = baseColumnNames;
    }

    public String getReferencedTableName() {
        return referencedTableName;
    }

    public void setReferencedTableName(String referencedTableName) {
        this.referencedTableName = referencedTableName;
    }

    public String getReferencedColumnNames() {
        return referencedColumnNames;
    }

    public void setReferencedColumnNames(String referencedColumnNames) {
        this.referencedColumnNames = referencedColumnNames;
    }

    public String getConstraintName() {
        return constraintName;
    }

    public void setConstraintName(String constraintName) {
        this.constraintName = constraintName;
    }

    public Boolean getDeferrable() {
        return deferrable;
    }

    public void setDeferrable(Boolean deferrable) {
        this.deferrable = deferrable;
    }

    public Boolean getInitiallyDeferred() {
        return initiallyDeferred;
    }

    public void setInitiallyDeferred(Boolean initiallyDeferred) {
        this.initiallyDeferred = initiallyDeferred;
    }

    public String[] generateStatements(Database database) throws UnsupportedChangeException {
        String[] statements = new String[]{
                "ALTER TABLE " + getBaseTableName() + " ADD CONSTRAINT " + getConstraintName() + " FOREIGN KEY (" + getBaseColumnNames() + ") REFERENCES " + getReferencedTableName() + "(" + getReferencedColumnNames() + ")",
        };
        if (database.supportsInitiallyDeferrableColumns()) {
            if (deferrable != null && deferrable) {
                statements[0] += " DEFERRABLE";
            }

            if (initiallyDeferred != null && initiallyDeferred) {
                statements[0] += " INITIALLY DEFERRED";
            }
        }

        return statements;
    }

    protected Change[] createInverses() {
        DropForeignKeyConstraintChange inverse = new DropForeignKeyConstraintChange();
        inverse.setBaseTableName(getBaseTableName());
        inverse.setConstraintName(getConstraintName());

        return new Change[]{
                inverse
        };
    }

    public String getConfirmationMessage() {
        return "Foreign Key Added";
    }

    public Element createNode(Document currentChangeLogFileDOM) {
        Element node = currentChangeLogFileDOM.createElement(getTagName());
        node.setAttribute("baseTableName", getBaseTableName());
        node.setAttribute("baseColumnNames", getBaseColumnNames());
        node.setAttribute("constraintName", getConstraintName());
        node.setAttribute("referencedTableName", getReferencedTableName());
        node.setAttribute("referencedColumnNames", getReferencedColumnNames());

        return node;
    }

}

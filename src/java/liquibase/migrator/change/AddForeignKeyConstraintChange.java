package liquibase.migrator.change;

import liquibase.database.*;
import liquibase.migrator.UnsupportedChangeException;
import org.w3c.dom.Element;
import org.w3c.dom.Document;

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

    private String[] generateCommonStatements(AbstractDatabase database) {
        return new String[] {
                "ALTER TABLE "+getBaseTableName()+" ADD CONSTRAINT "+getConstraintName()+" FOREIGN KEY ("+getBaseColumnNames()+") REFERENCES "+getReferencedTableName()+"("+getReferencedColumnNames()+")",
        };
    }

    public String[] generateStatements(MSSQLDatabase database) throws UnsupportedChangeException {
        return generateCommonStatements(database);
    }

    public String[] generateStatements(OracleDatabase database) throws UnsupportedChangeException {
        String[] strings = generateCommonStatements(database);
        if (deferrable != null && deferrable) {
            strings[0] += " DEFERRABLE";
        }

        if (initiallyDeferred != null && initiallyDeferred) {
            strings[0] += " INITIALLY DEFERRED";
        }
        return strings;
    }

    public String[] generateStatements(MySQLDatabase database) throws UnsupportedChangeException {
        return generateCommonStatements(database);
    }

    public String[] generateStatements(PostgresDatabase database) throws UnsupportedChangeException {
        return generateCommonStatements(database);
    }


    protected AbstractChange[] createInverses() {
        DropForeignKeyConstraintChange inverse = new DropForeignKeyConstraintChange();
        inverse.setBaseTableName(getBaseTableName());
        inverse.setConstraintName(getConstraintName());

        return new AbstractChange[] {
                inverse
        };
    }

    public String getConfirmationMessage() {
        return "Foreign Key Added";
    }

    public Element createNode(Document currentMigrationFileDOM) {
        Element node = currentMigrationFileDOM.createElement(getTagName());
        node.setAttribute("baseTableName", getBaseTableName());
        node.setAttribute("baseColumnNames", getBaseColumnNames());
        node.setAttribute("constraintName", getConstraintName());
        node.setAttribute("referencedTableName", getReferencedTableName());
        node.setAttribute("referencedColumnNames", getReferencedColumnNames());

        return node;
    }

}

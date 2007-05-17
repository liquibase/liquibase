package liquibase.migrator.change;

import liquibase.database.*;
import liquibase.migrator.UnsupportedChangeException;
import org.w3c.dom.Element;
import org.w3c.dom.Document;

public class DropForeignKeyConstraintChange extends AbstractChange {
    private String baseTableName;
    private String constraintName;

    public DropForeignKeyConstraintChange() {
        super("dropForeignKeyConstraint", "Drop Foreign Key Constraint");
    }

    public String getBaseTableName() {
        return baseTableName;
    }

    public void setBaseTableName(String baseTableName) {
        this.baseTableName = baseTableName;
    }

    public String getConstraintName() {
        return constraintName;
    }

    public void setConstraintName(String constraintName) {
        this.constraintName = constraintName;
    }

    private String[] generateCommonStatements() {
        return new String[] {
                "ALTER TABLE "+getBaseTableName()+" DROP CONSTRAINT "+getConstraintName(),
        };
    }

    public String[] generateStatements(MSSQLDatabase database) throws UnsupportedChangeException {
        return generateCommonStatements();
    }

    public String[] generateStatements(OracleDatabase database) throws UnsupportedChangeException {
        return generateCommonStatements();
    }

    public String[] generateStatements(MySQLDatabase database) throws UnsupportedChangeException {
        return new String[] {
                "ALTER TABLE "+getBaseTableName()+" DROP FOREIGN KEY "+getConstraintName(),
        };
    }

    public String[] generateStatements(PostgresDatabase database) throws UnsupportedChangeException {
        return generateCommonStatements();
    }

    public String getConfirmationMessage() {
        return "Foreign Key "+getConstraintName()+" was dropped";
    }

    public Element createNode(Document currentMigrationFileDOM) {
        Element node = currentMigrationFileDOM.createElement(getTagName());
        node.setAttribute("baseTableName", getBaseTableName());
        node.setAttribute("constraintName", getConstraintName());

        return node;
    }
}

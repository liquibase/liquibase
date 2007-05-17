package liquibase.migrator.change;

import liquibase.database.*;
import liquibase.migrator.UnsupportedChangeException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DropPrimaryKeyChange extends AbstractChange {
    private String tableName;
    private String constraintName;

    public DropPrimaryKeyChange() {
        super("dropPrimaryKey", "Drop Primary Key");
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

    private String[] generateCommonStatements() {
        return new String[] {
                "ALTER TABLE "+getTableName()+" DROP PRIMARY KEY",
        };
    }

    public String[] generateStatements(MSSQLDatabase database) throws UnsupportedChangeException {
        if (getConstraintName() == null) {
            throw new UnsupportedChangeException("MS-SQL requires a constraint name to drop the primary key");
        }
        return new String[] {
                "ALTER TABLE "+getTableName()+" DROP CONSTRAINT "+getConstraintName(),
        };
    }

    public String[] generateStatements(OracleDatabase database) throws UnsupportedChangeException {
        return generateCommonStatements();
    }

    public String[] generateStatements(MySQLDatabase database) throws UnsupportedChangeException {
        return generateCommonStatements();
    }

    public String[] generateStatements(PostgresDatabase database) throws UnsupportedChangeException {
        if (getConstraintName() == null) {
            throw new UnsupportedChangeException("PostgreSQL requires a constraint name to drop the primary key");
        }
        return new String[] {
                "ALTER TABLE "+getTableName()+" DROP CONSTRAINT "+getConstraintName(),
        };
    }

    public String getConfirmationMessage() {
        return "Primary Key Dropped";
    }

    public Element createNode(Document currentMigrationFileDOM) {
        Element node = currentMigrationFileDOM.createElement(getTagName());
        node.setAttribute("tableName", getTableName());
        return node;
    }
}

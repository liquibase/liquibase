package liquibase.migrator.change;

import liquibase.database.AbstractDatabase;
import liquibase.database.MSSQLDatabase;
import liquibase.database.PostgresDatabase;
import liquibase.migrator.UnsupportedChangeException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Removes an existing primary key.
 */
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

    public String[] generateStatements(AbstractDatabase database) throws UnsupportedChangeException {
        if (database instanceof MSSQLDatabase) {
            return generateMSSQLStatements();
        } else if (database instanceof PostgresDatabase) {
            return generatePostgresStatements();
        }

        return new String[]{
                "ALTER TABLE " + getTableName() + " DROP PRIMARY KEY",
        };
    }

    private String[] generateMSSQLStatements() throws UnsupportedChangeException {
        if (getConstraintName() == null) {
            throw new UnsupportedChangeException("MS-SQL requires a constraint name to drop the primary key");
        }
        return new String[]{
                "ALTER TABLE " + getTableName() + " DROP CONSTRAINT " + getConstraintName(),
        };
    }

    private String[] generatePostgresStatements() throws UnsupportedChangeException {
        if (getConstraintName() == null) {
            throw new UnsupportedChangeException("PostgreSQL requires a constraint name to drop the primary key");
        }
        return new String[]{
                "ALTER TABLE " + getTableName() + " DROP CONSTRAINT " + getConstraintName(),
        };
    }

    public String getConfirmationMessage() {
        return "Primary Key Dropped";
    }

    public Element createNode(Document currentChangeLogFileDOM) {
        Element node = currentChangeLogFileDOM.createElement(getTagName());
        node.setAttribute("tableName", getTableName());
        return node;
    }
}

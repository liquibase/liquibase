package liquibase.migrator.change;

import liquibase.database.Database;
import liquibase.database.MySQLDatabase;
import liquibase.database.structure.DatabaseObject;
import liquibase.database.structure.ForeignKey;
import liquibase.database.structure.Table;
import liquibase.migrator.exception.UnsupportedChangeException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashSet;
import java.util.Set;

/**
 * Drops an existing foreign key constraint.
 */
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

    public String[] generateStatements(Database database) throws UnsupportedChangeException {
        if (database instanceof MySQLDatabase) {
            return new String[]{ "ALTER TABLE " + getBaseTableName() + " DROP FOREIGN KEY " + getConstraintName(), };
        }

        return new String[]{
                "ALTER TABLE " + getBaseTableName() + " DROP CONSTRAINT " + getConstraintName(),
        };
    }

    public String getConfirmationMessage() {
        return "Foreign Key " + getConstraintName() + " was dropped";
    }

    public Element createNode(Document currentChangeLogFileDOM) {
        Element node = currentChangeLogFileDOM.createElement(getTagName());
        node.setAttribute("baseTableName", getBaseTableName());
        node.setAttribute("constraintName", getConstraintName());

        return node;
    }

    public Set<DatabaseObject> getAffectedDatabaseObjects() {
        Set<DatabaseObject> returnSet = new HashSet<DatabaseObject>();

        Table baseTable = new Table();
        baseTable.setName(baseTableName);
        returnSet.add(baseTable);

        ForeignKey fk = new ForeignKey();
        fk.setName(constraintName);
        fk.setForeignKeyTable(baseTable);
        returnSet.add(fk);

        return returnSet;

    }

}

package liquibase.change;

import liquibase.database.Database;
import liquibase.database.sql.DropForeignKeyConstraintStatement;
import liquibase.database.sql.SqlStatement;
import liquibase.database.structure.DatabaseObject;
import liquibase.database.structure.ForeignKey;
import liquibase.database.structure.Table;
import liquibase.exception.UnsupportedChangeException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashSet;
import java.util.Set;

/**
 * Drops an existing foreign key constraint.
 */
public class DropForeignKeyConstraintChange extends AbstractChange {
    private String baseTableSchemaName;
    private String baseTableName;
    private String constraintName;

    public DropForeignKeyConstraintChange() {
        super("dropForeignKeyConstraint", "Drop Foreign Key Constraint");
    }

    public String getBaseTableSchemaName() {
        return baseTableSchemaName;
    }

    public void setBaseTableSchemaName(String baseTableSchemaName) {
        this.baseTableSchemaName = baseTableSchemaName;
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

    public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
        return new SqlStatement[]{
                new DropForeignKeyConstraintStatement(
                        getBaseTableSchemaName() == null?database.getDefaultSchemaName():getBaseTableSchemaName(),
                        getBaseTableName(),
                        getConstraintName()),
        };
    }

    public String getConfirmationMessage() {
        return "Foreign key " + getConstraintName() + " dropped";
    }

    public Element createNode(Document currentChangeLogFileDOM) {
        Element node = currentChangeLogFileDOM.createElement(getTagName());

        if (getBaseTableSchemaName() != null) {
            node.setAttribute("baseTableSchemaName", getBaseTableSchemaName());
        }

        node.setAttribute("baseTableName", getBaseTableName());
        node.setAttribute("constraintName", getConstraintName());

        return node;
    }

    public Set<DatabaseObject> getAffectedDatabaseObjects() {
        Set<DatabaseObject> returnSet = new HashSet<DatabaseObject>();

        Table baseTable = new Table(getBaseTableName());
        returnSet.add(baseTable);

        ForeignKey fk = new ForeignKey();
        fk.setName(constraintName);
        fk.setForeignKeyTable(baseTable);
        returnSet.add(fk);

        return returnSet;

    }

}

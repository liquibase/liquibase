package liquibase.change;

import liquibase.database.Database;
import liquibase.database.sql.DropPrimaryKeyStatement;
import liquibase.database.sql.SqlStatement;
import liquibase.database.structure.DatabaseObject;
import liquibase.database.structure.Table;
import liquibase.exception.UnsupportedChangeException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashSet;
import java.util.Set;

/**
 * Removes an existing primary key.
 */
public class DropPrimaryKeyChange extends AbstractChange {
    private String schemaName;
    private String tableName;
    private String constraintName;

    public DropPrimaryKeyChange() {
        super("dropPrimaryKey", "Drop Primary Key");
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
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

    public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
        return new SqlStatement[]{
                new DropPrimaryKeyStatement(getSchemaName() == null?database.getDefaultSchemaName():getSchemaName(), getTableName(), getConstraintName()),
        };
    }

    public String getConfirmationMessage() {
        return "Primary key dropped from "+getTableName();
    }

    public Element createNode(Document currentChangeLogFileDOM) {
        Element node = currentChangeLogFileDOM.createElement(getTagName());
        if (getSchemaName() != null) {
            node.setAttribute("schemaName", getSchemaName());
        }
        
        node.setAttribute("tableName", getTableName());
        if (getConstraintName() != null) {
            node.setAttribute("constraintName", getConstraintName());
        }
        return node;
    }

    public Set<DatabaseObject> getAffectedDatabaseObjects() {

        Set<DatabaseObject> dbObjects = new HashSet<DatabaseObject>();

        Table table = new Table(getTableName());
        dbObjects.add(table);

        return dbObjects;

    }

}

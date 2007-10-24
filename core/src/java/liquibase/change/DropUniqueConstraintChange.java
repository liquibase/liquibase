package liquibase.change;

import liquibase.database.Database;
import liquibase.database.MySQLDatabase;
import liquibase.database.sql.RawSqlStatement;
import liquibase.database.sql.SqlStatement;
import liquibase.database.structure.DatabaseObject;
import liquibase.database.structure.Table;
import liquibase.exception.UnsupportedChangeException;
import liquibase.util.SqlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashSet;
import java.util.Set;

/**
 * Removes an existing unique constraint.
 */
public class DropUniqueConstraintChange extends AbstractChange {
    private String schemaName;
    private String tableName;
    private String constraintName;

    public DropUniqueConstraintChange() {
        super("dropUniqueConstraint", "Drop Unique Constraint");
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
        if (database instanceof MySQLDatabase) {
            return new SqlStatement[]{ new RawSqlStatement("ALTER TABLE " + database.escapeTableName(getSchemaName(), getTableName()) + " DROP KEY " + getConstraintName()), };
        }

        return new SqlStatement[]{
                new RawSqlStatement("ALTER TABLE " + database.escapeTableName(getSchemaName(), getTableName()) + " DROP CONSTRAINT " + getConstraintName()),
        };
    }

    public String getConfirmationMessage() {
        return "Unique constraint "+getConstraintName()+" dropped from "+getTableName();
    }

    public Element createNode(Document currentChangeLogFileDOM) {
        Element node = currentChangeLogFileDOM.createElement(getTagName());
        if (getSchemaName() != null) {
            node.setAttribute("schemaName", getSchemaName());
        }
        
        node.setAttribute("tableName", getTableName());
        node.setAttribute("constraintName", constraintName);
        return node;
    }

    public Set<DatabaseObject> getAffectedDatabaseObjects() {

        Set<DatabaseObject> returnSet = new HashSet<DatabaseObject>();

        Table table = new Table(getTableName());
        returnSet.add(table);

        return returnSet;

    }
}

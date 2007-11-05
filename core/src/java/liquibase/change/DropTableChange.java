package liquibase.change;

import liquibase.database.Database;
import liquibase.database.sql.DropTableStatement;
import liquibase.database.sql.SqlStatement;
import liquibase.database.structure.DatabaseObject;
import liquibase.database.structure.Table;
import liquibase.exception.UnsupportedChangeException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Drops an existing table.
 */
public class DropTableChange extends AbstractChange {

    private String schemaName;
    private String tableName;
    private Boolean cascadeConstraints;

    public DropTableChange() {
        super("dropTable", "Drop Table");
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

    public Boolean isCascadeConstraints() {
        return cascadeConstraints;
    }

    public void setCascadeConstraints(Boolean cascadeConstraints) {
        this.cascadeConstraints = cascadeConstraints;
    }

    public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
        boolean constraints = false;
        if (isCascadeConstraints() != null) {
            constraints = isCascadeConstraints();
        }
        
        return new SqlStatement[]{
                new DropTableStatement(getSchemaName(), getTableName(), constraints)
        };
    }

    public String getConfirmationMessage() {
        return "Table " + getTableName() + " dropped";
    }

    public Element createNode(Document currentChangeLogFileDOM) {
        Element element = currentChangeLogFileDOM.createElement("dropTable");
        if (getSchemaName() != null) {
            element.setAttribute("schemaName", getSchemaName());
        }

        element.setAttribute("tableName", getTableName());

        if (isCascadeConstraints() != null) {
            element.setAttribute("cascadeConstraints", isCascadeConstraints().toString());
        }

        return element;
    }

    public Set<DatabaseObject> getAffectedDatabaseObjects() {
        Table dbObject = new Table(getTableName());

        return new HashSet<DatabaseObject>(Arrays.asList(dbObject));
    }

}

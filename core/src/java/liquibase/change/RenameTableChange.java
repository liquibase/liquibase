package liquibase.change;

import liquibase.database.DB2Database;
import liquibase.database.Database;
import liquibase.database.sql.RenameTableStatement;
import liquibase.database.sql.ReorganizeTableStatement;
import liquibase.database.sql.SqlStatement;
import liquibase.database.structure.DatabaseObject;
import liquibase.database.structure.Table;
import liquibase.exception.UnsupportedChangeException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.*;

/**
 * Renames an existing table.
 */
public class RenameTableChange extends AbstractChange {

    private String schemaName;
    private String oldTableName;

    private String newTableName;

    public RenameTableChange() {
        super("renameTable", "Rename Table");
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public String getOldTableName() {
        return oldTableName;
    }

    public void setOldTableName(String oldTableName) {
        this.oldTableName = oldTableName;
    }

    public String getNewTableName() {
        return newTableName;
    }

    public void setNewTableName(String newTableName) {
        this.newTableName = newTableName;
    }


    public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
        List<SqlStatement> statements = new ArrayList<SqlStatement>();
        statements.add(new RenameTableStatement(getSchemaName(), getOldTableName(), getNewTableName()));
        if (database instanceof DB2Database) {
            statements.add(new ReorganizeTableStatement(getSchemaName(), getNewTableName()));
        }

        return statements.toArray(new SqlStatement[statements.size()]);
    }

    protected Change[] createInverses() {
        RenameTableChange inverse = new RenameTableChange();
        inverse.setSchemaName(getSchemaName());
        inverse.setOldTableName(getNewTableName());
        inverse.setNewTableName(getOldTableName());

        return new Change[]{
                inverse
        };
    }

    public String getConfirmationMessage() {
        return "Table " + oldTableName + " renamed to " + newTableName;
    }

    public Element createNode(Document currentChangeLogFileDOM) {
        Element element = currentChangeLogFileDOM.createElement("renameTable");
        if (getSchemaName() != null) {
            element.setAttribute("schemaName", getSchemaName());
        }

        element.setAttribute("oldTableName", getOldTableName());

        element.setAttribute("newTableName", getNewTableName());

        return element;
    }


    public Set<DatabaseObject> getAffectedDatabaseObjects() {
        Table oldTable = new Table(getOldTableName());

        Table newTable = new Table(getNewTableName());

        return new HashSet<DatabaseObject>(Arrays.asList(oldTable, newTable));
    }
}

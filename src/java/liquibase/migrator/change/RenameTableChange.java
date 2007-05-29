package liquibase.migrator.change;

import liquibase.database.MSSQLDatabase;
import liquibase.database.MySQLDatabase;
import liquibase.database.OracleDatabase;
import liquibase.database.PostgresDatabase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Renames an existing table.
 */
public class RenameTableChange extends AbstractChange {
    private String oldTableName;
    private String newTableName;

    public RenameTableChange() {
        super("renameTable", "Rename Table");
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

    public String[] generateStatements(MSSQLDatabase database) {
        return new String[]{"exec sp_rename '" + oldTableName + "', " + newTableName};
    }

    public String[] generateStatements(OracleDatabase database) {
        return new String[]{"RENAME " + oldTableName + " TO " + newTableName};
    }

    public String[] generateStatements(MySQLDatabase database) {
        return new String[]{"ALTER TABLE " + oldTableName + " RENAME " + newTableName};
    }

    public String[] generateStatements(PostgresDatabase database) {
        return new String[]{"ALTER TABLE " + oldTableName + " RENAME TO " + newTableName};
    }

    protected AbstractChange[] createInverses() {
        RenameTableChange inverse = new RenameTableChange();
        inverse.setOldTableName(getNewTableName());
        inverse.setNewTableName(getOldTableName());

        return new AbstractChange[]{
                inverse
        };
    }

    public String getConfirmationMessage() {
        return "Table with the name " + oldTableName + " has been renamed to " + newTableName;
    }

    public Element createNode(Document currentMigrationFileDOM) {
        Element element = currentMigrationFileDOM.createElement("renameTable");
        element.setAttribute("oldTableName", getOldTableName());
        element.setAttribute("newTableName", getNewTableName());

        return element;
    }
}

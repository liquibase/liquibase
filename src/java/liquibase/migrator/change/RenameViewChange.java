package liquibase.migrator.change;

import liquibase.database.MSSQLDatabase;
import liquibase.database.OracleDatabase;
import liquibase.database.MySQLDatabase;
import liquibase.database.PostgresDatabase;
import org.w3c.dom.Element;
import org.w3c.dom.Document;

public class RenameViewChange extends AbstractChange {
    private String oldViewName;
    private String newViewName;

    public RenameViewChange() {
        super("renameView", "Rename View");
    }

    public String getOldViewName() {
        return oldViewName;
    }

    public void setOldViewName(String oldViewName) {
        this.oldViewName = oldViewName;
    }

    public String getNewViewName() {
        return newViewName;
    }

    public void setNewViewName(String newViewName) {
        this.newViewName = newViewName;
    }

    public String[] generateStatements(MSSQLDatabase database) {
        return new String[] { "exec sp_rename '" + oldViewName + "', " + newViewName };
    }

    public String[] generateStatements(OracleDatabase database) {
        return new String[] { "RENAME " + oldViewName + " TO " + newViewName };
    }

    public String[] generateStatements(MySQLDatabase database) {
        return new String[] {  "RENAME TABLE " + oldViewName + " TO " + newViewName };
    }

    public String[] generateStatements(PostgresDatabase database) {
        return new String[] { "ALTER TABLE " + oldViewName + " RENAME TO " + newViewName };
    }

    protected AbstractChange[] createInverses() {
        RenameViewChange inverse = new RenameViewChange();
        inverse.setOldViewName(getNewViewName());
        inverse.setNewViewName(getOldViewName());

        return new AbstractChange[] {
                inverse
        };
    }

    public String getConfirmationMessage() {
        return "View with the name " + oldViewName + " has been renamed to " + newViewName;
    }

    public Element createNode(Document currentMigrationFileDOM) {
        Element element = currentMigrationFileDOM.createElement(getTagName());
        element.setAttribute("oldViewName", getOldViewName());
        element.setAttribute("newViewName", getNewViewName());

        return element;
    }

}

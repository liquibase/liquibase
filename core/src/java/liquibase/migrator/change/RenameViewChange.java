package liquibase.migrator.change;

import liquibase.database.*;
import liquibase.migrator.exception.UnsupportedChangeException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Renames an existing view.
 */
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

    public String[] generateStatements(Database database) throws UnsupportedChangeException {
        if (database instanceof MSSQLDatabase) {
            return new String[]{"exec sp_rename '" + oldViewName + "', " + newViewName};
        } else if (database instanceof MySQLDatabase) {
            return new String[]{"RENAME TABLE " + oldViewName + " TO " + newViewName};
        } else if (database instanceof PostgresDatabase) {
            return new String[]{"ALTER TABLE " + oldViewName + " RENAME TO " + newViewName};
        } else if (database instanceof DerbyDatabase) {
            throw new UnsupportedChangeException("Derby does not currently support renaming views");
        } else if (database instanceof HsqlDatabase) {
            throw new UnsupportedChangeException("HSQL does not currently support renaming views");
        } else if (database instanceof DB2Database) {
            throw new UnsupportedChangeException("DB2 does not currently support renaming views");
        } else if (database instanceof CacheDatabase) {
            throw new UnsupportedChangeException("Rename View not currently supported for Cache");
        }

        return new String[]{"RENAME " + oldViewName + " TO " + newViewName};
    }

    protected Change[] createInverses() {
        RenameViewChange inverse = new RenameViewChange();
        inverse.setOldViewName(getNewViewName());
        inverse.setNewViewName(getOldViewName());

        return new Change[]{
                inverse
        };
    }

    public String getConfirmationMessage() {
        return "View with the name " + oldViewName + " has been renamed to " + newViewName;
    }

    public Element createNode(Document currentChangeLogFileDOM) {
        Element element = currentChangeLogFileDOM.createElement(getTagName());
        element.setAttribute("oldViewName", getOldViewName());
        element.setAttribute("newViewName", getNewViewName());

        return element;
    }

}

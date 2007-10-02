package liquibase.change;

import liquibase.database.*;
import liquibase.database.sql.RawSqlStatement;
import liquibase.database.sql.SqlStatement;
import liquibase.database.structure.DatabaseObject;
import liquibase.database.structure.View;
import liquibase.exception.UnsupportedChangeException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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

    public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
        if (database instanceof MSSQLDatabase) {
            return new SqlStatement[]{new RawSqlStatement("exec sp_rename '" + oldViewName + "', " + newViewName)};
        } else if (database instanceof MySQLDatabase) {
            return new SqlStatement[]{new RawSqlStatement("RENAME TABLE " + oldViewName + " TO " + newViewName)};
        } else if (database instanceof PostgresDatabase) {
            return new SqlStatement[]{new RawSqlStatement("ALTER TABLE " + oldViewName + " RENAME TO " + newViewName)};
        } else if (database instanceof DerbyDatabase) {
            throw new UnsupportedChangeException("Derby does not currently support renaming views");
        } else if (database instanceof HsqlDatabase) {
            throw new UnsupportedChangeException("HSQL does not currently support renaming views");
        } else if (database instanceof DB2Database) {
            throw new UnsupportedChangeException("DB2 does not currently support renaming views");
        } else if (database instanceof CacheDatabase) {
            throw new UnsupportedChangeException("Rename View not currently supported for Cache");
        } else if (database instanceof FirebirdDatabase) {
            throw new UnsupportedChangeException("Rename View not currently supported for Firebird");
        }

        return new SqlStatement[]{new RawSqlStatement("RENAME " + oldViewName + " TO " + newViewName)};
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
        return "View " + oldViewName + " renamed to " + newViewName;
    }

    public Element createNode(Document currentChangeLogFileDOM) {
        Element element = currentChangeLogFileDOM.createElement(getTagName());
        element.setAttribute("oldViewName", getOldViewName());
        element.setAttribute("newViewName", getNewViewName());

        return element;
    }

    public Set<DatabaseObject> getAffectedDatabaseObjects() {
        View oldView = new View();
        oldView.setName(oldViewName);

        View newView = new View();
        newView.setName(newViewName);

        return new HashSet<DatabaseObject>(Arrays.asList(oldView, newView));
    }

}

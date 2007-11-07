package liquibase.change;

import liquibase.database.structure.DatabaseObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Set;

/**
 * Allows execution of arbitrary SQL.  This change can be used when existing changes are either don't exist,
 * are not flexible enough, or buggy. 
 */
public class RawSQLChange extends AbstractSQLChange {

    private String comments;
    public RawSQLChange() {
        super("sql", "Custom SQL");
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getConfirmationMessage() {
        return "Custom SQL executed";
    }

    public Element createNode(Document currentChangeLogFileDOM) {
        Element sqlElement = currentChangeLogFileDOM.createElement("sql");
        sqlElement.appendChild(currentChangeLogFileDOM.createTextNode(getSql()));

        return sqlElement;
    }


    public Set<DatabaseObject> getAffectedDatabaseObjects() {
        return null;
    }
}

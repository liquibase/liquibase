package liquibase.migrator.change;

import liquibase.database.Database;
import liquibase.database.structure.DatabaseObject;
import liquibase.database.structure.View;
import liquibase.migrator.exception.UnsupportedChangeException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Creats a new view.
 */
public class CreateViewChange extends AbstractChange {

    private String viewName;
    private String selectQuery;

    public CreateViewChange() {
        super("createView", "Create View");
    }

    public String getViewName() {
        return viewName;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    public String getSelectQuery() {
        return selectQuery;
    }

    public void setSelectQuery(String selectQuery) {
        this.selectQuery = selectQuery;
    }

    public String[] generateStatements(Database database) throws UnsupportedChangeException {
        return new String[]{
                "CREATE VIEW " + getViewName() + " AS " + selectQuery
        };
    }

    public String getConfirmationMessage() {
        return "View Created";
    }

    public Element createNode(Document currentChangeLogFileDOM) {
        Element element = currentChangeLogFileDOM.createElement("createView");
        element.setAttribute("viewName", getViewName());
        element.appendChild(currentChangeLogFileDOM.createTextNode(getSelectQuery()));

        return element;
    }

    protected Change[] createInverses() {
        DropViewChange inverse = new DropViewChange();
        inverse.setViewName(getViewName());

        return new Change[]{
                inverse
        };
    }

    public Set<DatabaseObject> getAffectedDatabaseObjects() {
        View dbObject = new View();
        dbObject.setName(viewName);

        return new HashSet<DatabaseObject>(Arrays.asList(dbObject));
    }

}

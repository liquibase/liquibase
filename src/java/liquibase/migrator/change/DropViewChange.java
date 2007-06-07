package liquibase.migrator.change;

import liquibase.database.Database;
import liquibase.migrator.UnsupportedChangeException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Drops an existing view.
 */
public class DropViewChange extends AbstractChange {
    private String viewName;

    public DropViewChange() {
        super("dropView", "Drop View");
    }

    public String getViewName() {
        return viewName;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    public String[] generateStatements(Database database) throws UnsupportedChangeException {
        return new String[]{
                "DROP VIEW " + viewName
        };
    }

    public String getConfirmationMessage() {
        return "View Dropped";
    }

    public Element createNode(Document currentChangeLogFileDOM) {
        Element element = currentChangeLogFileDOM.createElement("dropView");
        element.setAttribute("viewName", getViewName());

        return element;
    }
}

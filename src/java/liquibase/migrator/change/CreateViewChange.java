package liquibase.migrator.change;

import liquibase.database.MSSQLDatabase;
import liquibase.database.MySQLDatabase;
import liquibase.database.OracleDatabase;
import liquibase.database.PostgresDatabase;
import liquibase.migrator.UnsupportedChangeException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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

    public String[] generateStatements() {
        return new String[]{
                "CREATE VIEW " + getViewName() + " AS " + selectQuery
        };
    }

    public String[] generateStatements(MSSQLDatabase database) throws UnsupportedChangeException {
        return generateStatements();
    }

    public String[] generateStatements(OracleDatabase database) throws UnsupportedChangeException {
        return generateStatements();
    }

    public String[] generateStatements(MySQLDatabase database) throws UnsupportedChangeException {
        return generateStatements();
    }

    public String[] generateStatements(PostgresDatabase database) throws UnsupportedChangeException {
        return generateStatements();
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

    protected AbstractChange[] createInverses() {
        DropViewChange inverse = new DropViewChange();
        inverse.setViewName(getViewName());

        return new AbstractChange[]{
                inverse
        };
    }
}

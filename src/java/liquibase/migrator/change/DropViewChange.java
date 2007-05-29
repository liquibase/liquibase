package liquibase.migrator.change;

import liquibase.database.MSSQLDatabase;
import liquibase.database.MySQLDatabase;
import liquibase.database.OracleDatabase;
import liquibase.database.PostgresDatabase;
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

    private String[] generateStatements() {
        return new String[]{
                "DROP VIEW " + viewName
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
        return "View Dropped";
    }

    public Element createNode(Document currentMigrationFileDOM) {
        Element element = currentMigrationFileDOM.createElement("dropView");
        element.setAttribute("viewName", getViewName());

        return element;
    }
}

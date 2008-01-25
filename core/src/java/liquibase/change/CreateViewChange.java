package liquibase.change;

import liquibase.database.Database;
import liquibase.database.sql.CreateViewStatement;
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
 * Creats a new view.
 */
public class CreateViewChange extends AbstractChange {

    private String schemaName;
    private String viewName;
    private String selectQuery;
    private Boolean replaceIfExists;

    public CreateViewChange() {
        super("createView", "Create View");
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
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

    public Boolean getReplaceIfExists() {
        return replaceIfExists;
    }

    public void setReplaceIfExists(Boolean replaceIfExists) {
        this.replaceIfExists = replaceIfExists;
    }

    public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
        boolean replaceIfExists = false;
        if (getReplaceIfExists() != null && getReplaceIfExists()) {
            replaceIfExists = true;
        }
        return new SqlStatement[]{
                new CreateViewStatement(getSchemaName() == null?database.getDefaultSchemaName():getSchemaName(), getViewName(), getSelectQuery(), replaceIfExists),
        };
    }

    public String getConfirmationMessage() {
        return "View "+getViewName()+" created";
    }

    public Element createNode(Document currentChangeLogFileDOM) {
        Element element = currentChangeLogFileDOM.createElement("createView");

        if (getSchemaName() != null) {
            element.setAttribute("schemaName", getSchemaName());
        }

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

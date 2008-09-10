package liquibase.change;

import liquibase.database.Database;
import liquibase.database.sql.DropViewStatement;
import liquibase.database.sql.SqlStatement;
import liquibase.database.structure.DatabaseObject;
import liquibase.database.structure.View;
import liquibase.exception.UnsupportedChangeException;
import liquibase.exception.InvalidChangeDefinitionException;
import liquibase.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Drops an existing view.
 */
public class DropViewChange extends AbstractChange {
    private String schemaName;
    private String viewName;

    public DropViewChange() {
        super("dropView", "Drop View");
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

    public void validate(Database database) throws InvalidChangeDefinitionException {
        if (StringUtils.trimToNull(viewName) == null) {
            throw new InvalidChangeDefinitionException("viewName is required", this);
        }

    }

    public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
        return new SqlStatement[]{
                new DropViewStatement(getSchemaName() == null?database.getDefaultSchemaName():getSchemaName(), getViewName()),
        };
    }

    public String getConfirmationMessage() {
        return "View "+getViewName()+" dropped";
    }

    public Element createNode(Document currentChangeLogFileDOM) {
        Element element = currentChangeLogFileDOM.createElement("dropView");
        if (getSchemaName() != null) {
            element.setAttribute("schemaName", getSchemaName());
        }
        element.setAttribute("viewName", getViewName());

        return element;
    }

    public Set<DatabaseObject> getAffectedDatabaseObjects() {
        View dbObject = new View();
        dbObject.setName(viewName);

        return new HashSet<DatabaseObject>(Arrays.asList(dbObject));
    }

}

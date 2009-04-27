package liquibase.change;

import liquibase.database.Database;
import liquibase.database.statement.RenameViewStatement;
import liquibase.database.statement.SqlStatement;
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
 * Renames an existing view.
 */
public class RenameViewChange extends AbstractChange {
    private String schemaName;
    private String oldViewName;
    private String newViewName;

    public RenameViewChange() {
        super("renameView", "Rename View");
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = StringUtils.trimToNull(schemaName);
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

    public void validate(Database database) throws InvalidChangeDefinitionException {
        if (StringUtils.trimToNull(oldViewName) == null) {
            throw new InvalidChangeDefinitionException("oldViewName is required", this);
        }
        if (StringUtils.trimToNull(newViewName) == null) {
            throw new InvalidChangeDefinitionException("newViewName is required", this);
        }

    }

    public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
        return new SqlStatement[]{new RenameViewStatement(getSchemaName() == null?database.getDefaultSchemaName():getSchemaName(), getOldViewName(), getNewViewName())};
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
        Element element = currentChangeLogFileDOM.createElement(getChangeName());
        if (getSchemaName() != null) {
            element.setAttribute("schemaName", getSchemaName());
        }
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

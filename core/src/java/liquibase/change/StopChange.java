package liquibase.change;

import liquibase.database.sql.SqlStatement;
import liquibase.database.Database;
import liquibase.database.structure.DatabaseObject;
import liquibase.exception.UnsupportedChangeException;
import liquibase.exception.InvalidChangeDefinitionException;
import liquibase.util.StringUtils;
import org.w3c.dom.Node;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Set;
import java.sql.SQLException;

public class StopChange extends AbstractChange {

    private String message;

    public StopChange() {
        super("stop", "Stop Execution");
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = StringUtils.trimToNull(message);
    }

    public void validate(Database database) throws InvalidChangeDefinitionException {
        ;
    }

    public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
        throw new StopChangeException(getMessage());
    }

    public String getConfirmationMessage() {
        return "Changelog Execution Stopped";
    }

    public Node createNode(Document currentChangeLogDOM) {
        Element element = currentChangeLogDOM.createElement(getTagName());
        if (getMessage() != null) {
            element.setTextContent(getMessage());
        }
        return element;
    }

    public Set<DatabaseObject> getAffectedDatabaseObjects() {
        return null;
    }

    public static class StopChangeException extends RuntimeException {
        public StopChangeException(String message) {
            super(message);
        }
    }
}

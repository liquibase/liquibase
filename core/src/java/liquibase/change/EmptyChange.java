package liquibase.change;

import liquibase.database.statement.SqlStatement;
import liquibase.database.Database;
import liquibase.database.structure.DatabaseObject;
import liquibase.exception.UnsupportedChangeException;
import liquibase.exception.InvalidChangeDefinitionException;
import org.w3c.dom.Node;
import org.w3c.dom.Document;

import java.util.Set;

public class EmptyChange extends AbstractChange {
    public EmptyChange() {
        super("empty", "empty");
    }

    public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
        return new SqlStatement[0];
    }

    public String getConfirmationMessage() {
        return "Empty change did nothing";
    }

    public Node createNode(Document currentChangeLogDOM) {
        return currentChangeLogDOM.createElement("empty");
    }

    public Set<DatabaseObject> getAffectedDatabaseObjects() {
        return null;
    }

    public void validate(Database database) throws InvalidChangeDefinitionException {
        
    }
}

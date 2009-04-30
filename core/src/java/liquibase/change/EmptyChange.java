package liquibase.change;

import liquibase.database.Database;
import liquibase.database.statement.SqlStatement;
import liquibase.database.structure.DatabaseObject;
import liquibase.exception.InvalidChangeDefinitionException;
import liquibase.exception.UnsupportedChangeException;

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

    public Set<DatabaseObject> getAffectedDatabaseObjects() {
        return null;
    }

    public void validate(Database database) throws InvalidChangeDefinitionException {
        
    }
}

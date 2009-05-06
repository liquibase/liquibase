package liquibase.change;

import liquibase.database.Database;
import liquibase.exception.InvalidChangeDefinitionException;
import liquibase.exception.UnsupportedChangeException;
import liquibase.statement.SqlStatement;

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

    public void validate(Database database) throws InvalidChangeDefinitionException {
        
    }
}

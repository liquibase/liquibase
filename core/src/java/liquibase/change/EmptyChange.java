package liquibase.change;

import liquibase.database.Database;
import liquibase.exception.InvalidChangeDefinitionException;
import liquibase.statement.SqlStatement;

public class EmptyChange extends AbstractChange {
    public EmptyChange() {
        super("empty", "empty");
    }

    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[0];
    }

    public String getConfirmationMessage() {
        return "Empty change did nothing";
    }
}

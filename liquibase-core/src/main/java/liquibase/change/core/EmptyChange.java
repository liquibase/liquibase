package liquibase.change.core;

import liquibase.change.AbstractChange;
import liquibase.change.ChangeMetaData;
import liquibase.database.Database;
import liquibase.statement.SqlStatement;

public class EmptyChange extends AbstractChange {
    public EmptyChange() {
        super("empty", "empty", ChangeMetaData.PRIORITY_DEFAULT);
    }

    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[0];
    }

    public String getConfirmationMessage() {
        return "Empty change did nothing";
    }
}

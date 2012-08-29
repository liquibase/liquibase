package liquibase.change.core;

import liquibase.change.AbstractChange;
import liquibase.change.DatabaseChange;
import liquibase.change.ChangeMetaData;
import liquibase.database.Database;
import liquibase.statement.SqlStatement;

@DatabaseChange(name="empty", description = "empty", priority = ChangeMetaData.PRIORITY_DEFAULT)
public class EmptyChange extends AbstractChange {

    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[0];
    }

    public String getConfirmationMessage() {
        return "Empty change did nothing";
    }
}

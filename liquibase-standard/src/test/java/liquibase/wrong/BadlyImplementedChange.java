package liquibase.wrong;

import liquibase.change.AbstractChange;
import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.database.Database;
import liquibase.statement.SqlStatement;

@DatabaseChange(name = "createTable",
        description = "bla bla bla",
        priority = ChangeMetaData.PRIORITY_DEFAULT)
public class BadlyImplementedChange extends AbstractChange {

    // This test class does not implement the 'supports(Database)' method and may incorrectly override other databases changes causing unexpected behavior.

    @Override
    public String getConfirmationMessage() {
        return "";
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[0];
    }
}

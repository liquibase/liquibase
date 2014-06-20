package liquibase.change.core;

import liquibase.change.AbstractChange;
import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.executor.ExecutionOptions;
import liquibase.statement.SqlStatement;

@DatabaseChange(name="empty", description = "empty", priority = ChangeMetaData.PRIORITY_DEFAULT)
public class EmptyChange extends AbstractChange {

    @Override
    public SqlStatement[] generateStatements(ExecutionOptions options) {
        return new SqlStatement[0];
    }

    @Override
    public String getConfirmationMessage() {
        return "Empty change did nothing";
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
}

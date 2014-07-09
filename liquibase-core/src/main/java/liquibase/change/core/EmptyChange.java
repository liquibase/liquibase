package liquibase.change.core;

import liquibase.change.AbstractChange;
import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import  liquibase.ExecutionEnvironment;
import liquibase.statement.Statement;

@DatabaseChange(name="empty", description = "empty", priority = ChangeMetaData.PRIORITY_DEFAULT)
public class EmptyChange extends AbstractChange {

    @Override
    public Statement[] generateStatements(ExecutionEnvironment env) {
        return new Statement[0];
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

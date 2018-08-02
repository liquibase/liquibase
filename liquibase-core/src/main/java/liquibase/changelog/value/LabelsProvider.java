package liquibase.changelog.value;

import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.statement.core.MarkChangeSetRanStatement;

public class LabelsProvider implements ChangeLogColumnValueProvider {
    @Override
    public Object getValue(MarkChangeSetRanStatement statement, Database database) {
        ChangeSet changeSet = statement.getChangeSet();

        return ((changeSet.getLabels() == null) || changeSet.getLabels().isEmpty() ) ? null : changeSet.getLabels().toString();
    }
}

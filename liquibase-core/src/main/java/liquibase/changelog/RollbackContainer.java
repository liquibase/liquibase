package liquibase.changelog;

import java.util.ArrayList;
import java.util.List;

import liquibase.change.Change;
import liquibase.serializer.AbstractLiquibaseSerializable;

public class RollbackContainer extends AbstractLiquibaseSerializable {
    private List<Change> changes = new ArrayList<Change>();

    @Override
    public String getSerializedObjectName() {
        return "rollback";
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

    public List<Change> getChanges() {
        return changes;
    }

    public void setChanges(List<Change> changes) {
        this.changes = changes;
    }
}

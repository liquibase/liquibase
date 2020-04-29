package liquibase.changelog;

import liquibase.change.Change;
import liquibase.serializer.AbstractLiquibaseSerializable;

import java.util.ArrayList;
import java.util.List;

public class RollbackContainer extends AbstractLiquibaseSerializable {
    private List<Change> changes = new ArrayList<>();

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

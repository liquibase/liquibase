package liquibase.changelog;

import liquibase.ChangeSet;
import liquibase.change.Change;
import liquibase.change.ColumnConfig;

public interface ChangeLogSerializer {
    String serialize(Change change);

    String serialize(ColumnConfig columnConfig);

    String serialize(ChangeSet changeSet);
}

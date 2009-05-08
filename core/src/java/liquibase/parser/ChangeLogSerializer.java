package liquibase.parser;

import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.changelog.ChangeSet;

public interface ChangeLogSerializer {
    String serialize(Change change);

    String serialize(ColumnConfig columnConfig);

    String serialize(ChangeSet changeSet);
}

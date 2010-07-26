package liquibase.serializer;

import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.sql.visitor.SqlVisitor;

public interface ChangeLogSerializer {

    String[] getValidFileExtensions();

    String serialize(DatabaseChangeLog databaseChangeLog);

    String serialize(ChangeSet changeSet);
    
    String serialize(Change change);

    String serialize(SqlVisitor visitor);

    String serialize(ColumnConfig columnConfig);

}

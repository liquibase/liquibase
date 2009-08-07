package liquibase.serializer;

import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;

public class MockChangeLogSerializer implements ChangeLogSerializer {

    private String[] validExtensions;

    public MockChangeLogSerializer(String... validExtensions) {
        this.validExtensions = validExtensions;
    }

    public String[] getValidFileExtensions() {
        return validExtensions;
    }

    public String serialize(DatabaseChangeLog databaseChangeLog) {
        return null;
    }

    public String serialize(ChangeSet changeSet) {
        return null;
    }

    public String serialize(Change change) {
        return null;
    }

    public String serialize(ColumnConfig columnConfig) {
        return null;
    }
}

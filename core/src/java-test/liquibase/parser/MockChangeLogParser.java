package liquibase.parser;

import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.ChangeSet;
import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.serializer.ChangeLogSerializer;
import liquibase.resource.FileOpener;
import liquibase.exception.ChangeLogParseException;

import java.util.Map;

public class MockChangeLogParser implements ChangeLogParser {

    private String[] validExtensions;

    public MockChangeLogParser(String... validExtensions) {
        this.validExtensions = validExtensions;
    }

    public String[] getValidFileExtensions() {
        return validExtensions;
    }

    public DatabaseChangeLog parse(String physicalChangeLogLocation, Map<String, Object> changeLogParameters, FileOpener fileOpener) throws ChangeLogParseException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
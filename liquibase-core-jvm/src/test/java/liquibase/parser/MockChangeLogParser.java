package liquibase.parser;

import liquibase.changelog.DatabaseChangeLog;
import liquibase.resource.ResourceAccessor;
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

    public DatabaseChangeLog parse(String physicalChangeLogLocation, Map<String, Object> changeLogParameters, ResourceAccessor resourceAccessor) throws ChangeLogParseException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
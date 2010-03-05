package liquibase.parser;

import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.ChangeLogParameters;
import liquibase.exception.ChangeLogParseException;
import liquibase.resource.ResourceAccessor;

public class MockChangeLogParser implements ChangeLogParser {

    private String[] validExtensions;

    public MockChangeLogParser(String... validExtensions) {
        this.validExtensions = validExtensions;
    }

    public int getPriority() {
        return PRIORITY_DEFAULT;
    }
    

    public String[] getValidFileExtensions() {
        return validExtensions;
    }

    public DatabaseChangeLog parse(String physicalChangeLogLocation, ChangeLogParameters changeLogParameters, ResourceAccessor resourceAccessor) throws ChangeLogParseException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
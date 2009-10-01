package liquibase.parser;

import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.ChangeLogParameter;
import liquibase.exception.ChangeLogParseException;
import liquibase.resource.ResourceAccessor;

import java.util.Map;
import java.util.List;

public class MockChangeLogParser implements ChangeLogParser {

    private String[] validExtensions;

    public MockChangeLogParser(String... validExtensions) {
        this.validExtensions = validExtensions;
    }

    public String[] getValidFileExtensions() {
        return validExtensions;
    }

    public DatabaseChangeLog parse(String physicalChangeLogLocation, List<ChangeLogParameter> changeLogParameters, ResourceAccessor resourceAccessor) throws ChangeLogParseException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
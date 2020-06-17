package liquibase.parser;

import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.exception.ChangeLogParseException;
import liquibase.resource.ResourceAccessor;
import liquibase.servicelocator.LiquibaseService;

@LiquibaseService(skip = true)
public class MockChangeLogParser implements ChangeLogParser {

    private String[] validExtensions;

    public MockChangeLogParser(String... validExtensions) {
        this.validExtensions = validExtensions;
    }

    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }
    @Override
    public boolean supports(String changeLogFile, ResourceAccessor resourceAccessor) {
        for (String ext : validExtensions) {
            if (changeLogFile.endsWith("."+validExtensions)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public DatabaseChangeLog parse(String physicalChangeLogLocation, ChangeLogParameters changeLogParameters, ResourceAccessor resourceAccessor) throws ChangeLogParseException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
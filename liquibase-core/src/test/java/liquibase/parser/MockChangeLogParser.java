package liquibase.parser;

import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.exception.ChangeLogParseException;
import liquibase.resource.ResourceAccessor;
import liquibase.servicelocator.LiquibaseService;

import java.util.HashMap;
import java.util.Map;

@LiquibaseService(skip = true)
public class MockChangeLogParser implements ChangeLogParser {

    private String[] validExtensions;
    public Map<String, DatabaseChangeLog> changeLogs = new HashMap<>();

    public MockChangeLogParser() {
    }

    public MockChangeLogParser(String... validExtensions) {
        this.validExtensions = validExtensions;
    }

    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    @Override
    public boolean supports(String changeLogFile, ResourceAccessor resourceAccessor) {
        if (changeLogs.containsKey(changeLogFile)) {
            return true;
        }
        for (String ext : validExtensions) {
            if (changeLogFile.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public DatabaseChangeLog parse(String physicalChangeLogLocation, ChangeLogParameters changeLogParameters,
                                   ResourceAccessor resourceAccessor) throws ChangeLogParseException {
        return changeLogs.get(physicalChangeLogLocation);
    }
}

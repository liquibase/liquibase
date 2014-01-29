package liquibase.parser;

import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.exception.ChangeLogParseException;
import liquibase.resource.ResourceAccessor;
import liquibase.servicelocator.PrioritizedService;

public interface ChangeLogParser extends PrioritizedService, LiquibaseParser {

    public DatabaseChangeLog parse(String physicalChangeLogLocation, ChangeLogParameters changeLogParameters, ResourceAccessor resourceAccessor) throws ChangeLogParseException;

    boolean supports(String changeLogFile, ResourceAccessor resourceAccessor);
}

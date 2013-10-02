package liquibase.parser;

import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.ChangeLogParameters;
import liquibase.exception.ChangeLogParseException;
import liquibase.resource.ResourceAccessor;
import liquibase.servicelocator.PrioritizedService;

public interface ChangeLogParser extends PrioritizedService {

    public DatabaseChangeLog parse(String physicalChangeLogLocation, ChangeLogParameters changeLogParameters, ResourceAccessor resourceAccessor) throws ChangeLogParseException;

    boolean supports(String changeLogFile, ResourceAccessor resourceAccessor);
}

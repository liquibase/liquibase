package liquibase.command.core;

import liquibase.Scope;
import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.command.AbstractCommandStep;
import liquibase.exception.LiquibaseException;
import liquibase.parser.ChangeLogParser;
import liquibase.parser.ChangeLogParserFactory;
import liquibase.resource.ResourceAccessor;

public abstract class AbstractHubChangelogCommandStep extends AbstractCommandStep {

    protected DatabaseChangeLog parseChangeLogFile(String changeLogFile) throws LiquibaseException {
        ResourceAccessor resourceAccessor = Scope.getCurrentScope().getResourceAccessor();
        ChangeLogParser parser = ChangeLogParserFactory.getInstance().getParser(changeLogFile, resourceAccessor);
        ChangeLogParameters changeLogParameters = new ChangeLogParameters();
        return parser.parse(changeLogFile, changeLogParameters, resourceAccessor);
    }
}

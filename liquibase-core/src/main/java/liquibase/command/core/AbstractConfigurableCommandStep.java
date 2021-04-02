package liquibase.command.core;

import liquibase.Scope;
import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.command.AbstractCommandStep;
import liquibase.command.CommandDefinition;
import liquibase.command.CommandScope;
import liquibase.command.CommandStep;
import liquibase.exception.CommandValidationException;
import liquibase.exception.LiquibaseException;
import liquibase.integration.commandline.CommandLineResourceAccessor;
import liquibase.parser.ChangeLogParser;
import liquibase.parser.ChangeLogParserFactory;
import liquibase.resource.CompositeResourceAccessor;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.resource.ResourceAccessor;
import liquibase.util.FileUtil;
import liquibase.util.StringUtil;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Convenience base class for {@link CommandStep} implementations.
 */
public abstract class AbstractConfigurableCommandStep extends AbstractCommandStep {
    public DatabaseChangeLog parseChangeLogFile(String changeLogFile) throws LiquibaseException {
        //
        // Parse the file to get the DatabaseChangeLog and add it to the CommandScope
        //
        ResourceAccessor resourceAccessor = Scope.getCurrentScope().getResourceAccessor();
        ChangeLogParser parser = ChangeLogParserFactory.getInstance().getParser(changeLogFile, resourceAccessor);
        ChangeLogParameters changeLogParameters = new ChangeLogParameters();
        return parser.parse(changeLogFile, changeLogParameters, resourceAccessor);
    }
}

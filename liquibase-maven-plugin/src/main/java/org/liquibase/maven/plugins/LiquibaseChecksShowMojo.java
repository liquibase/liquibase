package org.liquibase.maven.plugins;

import liquibase.Liquibase;
import liquibase.Scope;
import liquibase.command.CommandScope;
import liquibase.exception.CommandExecutionException;
import org.apache.commons.lang3.StringUtils;

/**
 * List available checks, their configuration options, and current settings
 *
 * @goal checks.show
 */
public class LiquibaseChecksShowMojo extends AbstractLiquibaseChecksMojo {

    @Override
    protected void performLiquibaseTask(Liquibase liquibase) throws CommandExecutionException {
        try {
            CommandScope liquibaseCommand = new CommandScope("checks", "show");
            if (! doesMarkerClassExist()) {
                throw new CommandExecutionException(Scope.CHECKS_MESSAGE);
            }
            if (StringUtils.isNotEmpty(checksSettingsFile)) {
                liquibaseCommand.addArgumentValue("checksSettingsFile", checksSettingsFile);
            }
            liquibaseCommand.addArgumentValue("checksIntegration", "maven");
            liquibaseCommand.execute();
        } catch (IllegalArgumentException e) {
            throw new CommandExecutionException(Scope.CHECKS_MESSAGE);
        }
    }
}

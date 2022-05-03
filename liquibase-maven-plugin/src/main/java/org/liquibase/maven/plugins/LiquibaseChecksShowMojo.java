package org.liquibase.maven.plugins;

import liquibase.Liquibase;
import liquibase.command.CommandScope;
import liquibase.command.CommonArgumentNames;
import liquibase.exception.CommandExecutionException;
import liquibase.exception.LiquibaseException;
import liquibase.util.StringUtil;
import org.liquibase.maven.property.PropertyElement;

/**
 * List available checks, their configuration options, and current settings
 *
 * @goal checks.show
 */
public class LiquibaseChecksShowMojo extends AbstractLiquibaseChecksMojo {

    @Override
    protected void performLiquibaseTask(Liquibase liquibase) throws CommandExecutionException {
        CommandScope liquibaseCommand = new CommandScope("checks", "show");
        if (StringUtil.isNotEmpty(checksSettingsFile)) {
            liquibaseCommand.addArgumentValue("checksSettingsFile", checksSettingsFile);
        }
        liquibaseCommand.addArgumentValue("checksIntegration", "maven");
        liquibaseCommand.execute();
    }
}

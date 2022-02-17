package org.liquibase.maven.plugins;

import liquibase.command.CommandScope;
import liquibase.command.CommonArgumentNames;
import liquibase.exception.CommandExecutionException;
import liquibase.util.StringUtil;
import org.liquibase.maven.property.PropertyElement;

/**
 * @goal checks.run
 */
public class LiquibaseChecksRunMojo extends AbstractLiquibaseChecksMojo {

    /**
     * Specifies the <i>changelog</i> file for Liquibase Quality Checks to use.
     *
     * @parameter property="liquibase.changeLogFile"
     */
    @PropertyElement
    protected String changeLogFile;

    @Override
    protected void performChecksTask() throws CommandExecutionException {
        CommandScope liquibaseCommand = new CommandScope("checks", "run");
        liquibaseCommand.addArgumentValue(CommonArgumentNames.CHANGELOG_FILE.getArgumentName(), changeLogFile);
        if (StringUtil.isNotEmpty(checksSettingsFile)) {
            liquibaseCommand.addArgumentValue("checksSettingsFile", checksSettingsFile);
        }
        liquibaseCommand.execute();
    }

    @Override
    public boolean shouldLoadLiquibaseProperties() {
        return false;
    }

    @Override
    public boolean databaseConnectionRequired() {
        return false;
    }
}

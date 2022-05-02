package org.liquibase.maven.plugins;

import liquibase.Liquibase;
import liquibase.command.CommandScope;
import liquibase.command.CommonArgumentNames;
import liquibase.exception.CommandExecutionException;
import liquibase.util.StringUtil;
import org.liquibase.maven.property.PropertyElement;

/**
 * Check the changelog for issues
 *
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

    /**
     * Specifies the <i>format</i> file for Liquibase Quality Checks to use. If not specified, the default
     * format will be used.
     *
     * @parameter property="liquibase.format"
     */
    @PropertyElement
    protected String format;

    @Override
    protected void performLiquibaseTask(Liquibase liquibase) throws CommandExecutionException {
        CommandScope liquibaseCommand = new CommandScope("checks", "run");
        liquibaseCommand.addArgumentValue(CommonArgumentNames.CHANGELOG_FILE.getArgumentName(), changeLogFile);
        if (StringUtil.isNotEmpty(checksSettingsFile)) {
            liquibaseCommand.addArgumentValue("checksSettingsFile", checksSettingsFile);
        }
        if (StringUtil.isNotEmpty(format)) {
            liquibaseCommand.addArgumentValue("format", format);
        }
        liquibaseCommand.addArgumentValue("checksIntegration", "maven");
        liquibaseCommand.execute();
    }
}

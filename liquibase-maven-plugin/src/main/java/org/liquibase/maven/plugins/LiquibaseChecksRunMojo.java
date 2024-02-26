package org.liquibase.maven.plugins;

import liquibase.Liquibase;
import liquibase.command.CommandScope;
import liquibase.command.CommonArgumentNames;
import liquibase.exception.CommandExecutionException;
import liquibase.util.StringUtil;
import org.liquibase.maven.property.PropertyElement;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

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

    /**
     * The Liquibase component to run checks against, which can be a comma separated list
     *
     * @parameter property="liquibase.checksScope"
     */
    @PropertyElement
    protected String checksScope;

    /**
     * Allows automatic backup and updating of liquibase.checks.conf file when new quality checks are available. Options: [on|off]
     *
     * @parameter property="liquibase.autoUpdate"
     */
    @PropertyElement
    protected String autoUpdate;

    /**
     * Comma-separated list of one or more enabled checks to run. If not specified, all enabled checks will run. Example: --check-name=shortname1,shortname2,shortname3
     *
     * @parameter property="liquibase.checkName"
     */
    @PropertyElement
    protected String checkName;


    /**
     * The schemas to snapshot
     *
     * @parameter property="liquibase.schemas"
     */
    @PropertyElement
    protected String schemas;

    /**
     * The JDBC driver properties file
     *
     * @parameter property="liquibase.driverPropertiesFile"
     */
    @PropertyElement
    protected String driverPropertiesFile;

    /**
     * @parameter property="liquibase.outputFile"
     */
    @PropertyElement
    protected File outputFile;

    /**
     * @parameter property="liquibase.sqlParserExceptionLogAtLevel"
     */
    @PropertyElement
    protected String sqlParserExceptionLogAtLevel;

    @Override
    protected void performLiquibaseTask(Liquibase liquibase) throws CommandExecutionException {
        CommandScope liquibaseCommand = new CommandScope("checks", "run");
        addArgumentIfNotEmpty(liquibaseCommand, changeLogFile, CommonArgumentNames.CHANGELOG_FILE.getArgumentName());
        addArgumentIfNotEmpty(liquibaseCommand, checksSettingsFile, "checksSettingsFile");
        addArgumentIfNotEmpty(liquibaseCommand, format, "format");
        addArgumentIfNotEmpty(liquibaseCommand, checksScope, "checksScope");
        addArgumentIfNotEmpty(liquibaseCommand, autoUpdate, "autoUpdate");
        addArgumentIfNotEmpty(liquibaseCommand, checkName, "checkName");
        addArgumentIfNotEmpty(liquibaseCommand, username, "username");
        addArgumentIfNotEmpty(liquibaseCommand, password, "password");
        addArgumentIfNotEmpty(liquibaseCommand, url, "url");
        addArgumentIfNotEmpty(liquibaseCommand, schemas, "schemas");
        addArgumentIfNotEmpty(liquibaseCommand, defaultSchemaName, "defaultSchemaName");
        addArgumentIfNotEmpty(liquibaseCommand, defaultCatalogName, "defaultCatalogName");
        addArgumentIfNotEmpty(liquibaseCommand, driver, "driver");
        addArgumentIfNotEmpty(liquibaseCommand, driverPropertiesFile, "driverPropertiesFile");
        addArgumentIfNotEmpty(liquibaseCommand, sqlParserExceptionLogAtLevel, "sqlParserExceptionLogAtLevel");
        if (outputFile != null) {
            try {
                liquibaseCommand.setOutput(Files.newOutputStream(outputFile.toPath()));
            } catch (IOException e) {
                throw new CommandExecutionException(e);
            }
        }
        liquibaseCommand.addArgumentValue("checksIntegration", "maven");
        liquibaseCommand.execute();
    }

    private void addArgumentIfNotEmpty(CommandScope commandScope, String argument, String name) {
        if (StringUtil.isNotEmpty(argument)) {
            commandScope.addArgumentValue(name, argument);
        }
    }
}

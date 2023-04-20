package org.liquibase.maven.plugins;

import liquibase.Liquibase;
import liquibase.command.CommandScope;
import liquibase.command.CommonArgumentNames;
import liquibase.exception.CommandExecutionException;
import liquibase.util.StringUtil;
import org.liquibase.maven.property.PropertyElement;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
     * Username to use to connect to the database
     *
     * @parameter property="liquibase.username"
     */
    @PropertyElement
    protected String username;

    /**
     * Password to use to connect to the database
     *
     * @parameter property="liquibase.password"
     */
    @PropertyElement
    protected String password;

    /**
     * The JDBC database connection URL.  One of --changelog-file or --url is required.
     *
     * @parameter property="liquibase.url"
     */
    @PropertyElement
    protected String url;

    /**
     * The schemas to snapshot
     *
     * @parameter property="liquibase.schemas"
     */
    @PropertyElement
    protected String schemas;

    /**
     * The default schema name to use for the database connection
     *
     * @parameter property="liquibase.defaultSchemaName"
     */
    @PropertyElement
    protected String defaultSchemaName;

    /**
     * The default catalog name to use for the database connection
     *
     * @parameter property="liquibase.defaultCatalogName"
     */
    @PropertyElement
    protected String defaultCatalogName;

    /**
     * The JDBC driver class
     *
     * @parameter property="liquibase.driver"
     */
    @PropertyElement
    protected String driver;

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

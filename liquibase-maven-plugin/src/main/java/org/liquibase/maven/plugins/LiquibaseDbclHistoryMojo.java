package org.liquibase.maven.plugins;

import liquibase.Liquibase;
import liquibase.command.CommandScope;
import liquibase.command.core.helpers.DbUrlConnectionArgumentsCommandStep;
import liquibase.exception.CommandExecutionException;
import liquibase.exception.LiquibaseException;
import org.liquibase.maven.property.PropertyElement;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * List all rows from the Liquibase Pro 'DATABASECHANGELOGHISTORY' tracking table.
 * @goal dbclHistory
 */
public class LiquibaseDbclHistoryMojo extends AbstractLiquibaseMojo {
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
     * Set to 'true' to output all data from EXECUTEDSQL and EXTENSIONS columns
     * @parameter property="liquibase.verbose"
     */
    @PropertyElement
    protected Boolean verbose;

    /**
     * Sets the output method to JSON or JSON_PRETTY
     * @parameter property="liquibase.format"
     */
    @PropertyElement
    protected String format;

    @Override
    protected void performLiquibaseTask(Liquibase liquibase) throws LiquibaseException {
        CommandScope liquibaseCommand = new CommandScope("dbclHistory");
        liquibaseCommand.addArgumentValue("format", format);
        liquibaseCommand.addArgumentValue("verbose", verbose);
        liquibaseCommand.addArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG, username);
        liquibaseCommand.addArgumentValue(DbUrlConnectionArgumentsCommandStep.PASSWORD_ARG, password);
        liquibaseCommand.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, url);
        liquibaseCommand.addArgumentValue(DbUrlConnectionArgumentsCommandStep.DEFAULT_SCHEMA_NAME_ARG, defaultSchemaName);
        liquibaseCommand.addArgumentValue(DbUrlConnectionArgumentsCommandStep.DEFAULT_CATALOG_NAME_ARG, defaultCatalogName);
        liquibaseCommand.addArgumentValue(DbUrlConnectionArgumentsCommandStep.DRIVER_ARG, driver);
        liquibaseCommand.addArgumentValue(DbUrlConnectionArgumentsCommandStep.DRIVER_PROPERTIES_FILE_ARG, driverPropertiesFile);
        if (outputFile != null) {
            try {
                liquibaseCommand.setOutput(Files.newOutputStream(outputFile.toPath()));
            } catch (IOException e) {
                throw new CommandExecutionException(e);
            }
        }
        liquibaseCommand.execute();
    }
}

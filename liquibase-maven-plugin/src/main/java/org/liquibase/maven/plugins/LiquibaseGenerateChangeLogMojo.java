package org.liquibase.maven.plugins;

import liquibase.Liquibase;
import liquibase.configuration.GlobalConfiguration;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.database.Database;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.StandardObjectChangeFilter;
import liquibase.exception.LiquibaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.integration.commandline.CommandLineUtils;
import liquibase.util.StringUtil;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

/**
 * <p>Generates a changelog based on the current database schema. Typically used when
 * beginning to use Liquibase on an existing project and database schema.</p>
 *
 * @author Marcello Teodori
 * @goal generateChangeLog
 * @since 2.0.6
 */
public class LiquibaseGenerateChangeLogMojo extends
        AbstractLiquibaseMojo {

    /**
     * List of diff types to include in Change Log expressed as a comma separated list from: tables, views, columns, indexes, foreignkeys, primarykeys, uniqueconstraints, data.
     * If this is null then the default types will be: tables, views, columns, indexes, foreignkeys, primarykeys, uniqueconstraints
     *
     * @parameter property="liquibase.diffTypes"
     */
    protected String diffTypes;

    /**
     * Directory where insert statement csv files will be kept.
     *
     * @parameter property="liquibase.dataDir"
     */
    protected String dataDir;

    /**
     * The author to be specified for Change Sets in the generated Change Log.
     *
     * @parameter property="liquibase.changeSetAuthor"
     */
    protected String changeSetAuthor;

    /**
     * are required. If no context is specified then ALL contexts will be executed.
     * @parameter property="liquibase.contexts" default-value=""
     */
    protected String contexts;

    /**
     * The execution context to be used for Change Sets in the generated Change Log, which can be "," separated if multiple contexts.
     *
     * @parameter property="liquibase.changeSetContext"
     */
    protected String changeSetContext;

    /**
     * The target change log file to output to. If this is null then the output will be to the screen.
     *
     * @parameter property="liquibase.outputChangeLogFile"
     */
    protected String outputChangeLogFile;


    /**
     * Objects to be excluded from the changelog. Example filters: "table_name", "table:main_.*", "column:*._lock, table:primary.*".
     *
     * @parameter property="liquibase.diffExcludeObjects"
     */
    protected String diffExcludeObjects;

    /**
     * Objects to be included in the changelog. Example filters: "table_name", "table:main_.*", "column:*._lock, table:primary.*".
     *
     * @parameter property="liquibase.diffIncludeObjects"
     */
    protected String diffIncludeObjects;


    @Override
	protected void performLiquibaseTask(Liquibase liquibase)
			throws LiquibaseException {

        ClassLoader cl = null;
        try {
            cl = getClassLoaderIncludingProjectClasspath();
            Thread.currentThread().setContextClassLoader(cl);
        }
        catch (MojoExecutionException e) {
            throw new LiquibaseException("Could not create the class loader, " + e, e);
        }

        Database database = liquibase.getDatabase();

        getLog().info("Generating Change Log from database " + database.toString());
        try {
            DiffOutputControl diffOutputControl = new DiffOutputControl(outputDefaultCatalog, outputDefaultSchema, true, null);
            if ((diffExcludeObjects != null) && (diffIncludeObjects != null)) {
                throw new UnexpectedLiquibaseException("Cannot specify both excludeObjects and includeObjects");
            }
            if (diffExcludeObjects != null) {
                diffOutputControl.setObjectChangeFilter(new StandardObjectChangeFilter(StandardObjectChangeFilter.FilterType.EXCLUDE, diffExcludeObjects));
            }
            if (diffIncludeObjects != null) {
                diffOutputControl.setObjectChangeFilter(new StandardObjectChangeFilter(StandardObjectChangeFilter.FilterType.INCLUDE, diffIncludeObjects));
            }

            //
            // Set the global configuration option based on presence of the dataOutputDirectory
            //
            boolean b = dataDir != null;
            LiquibaseConfiguration.getInstance().getConfiguration(GlobalConfiguration.class).setShouldSnapshotData(b);

            CommandLineUtils.doGenerateChangeLog(outputChangeLogFile, database, defaultCatalogName, defaultSchemaName, StringUtil.trimToNull(diffTypes),
                    StringUtil.trimToNull(changeSetAuthor), StringUtil.trimToNull(changeSetContext), StringUtil.trimToNull(dataDir), diffOutputControl);
            getLog().info("Output written to Change Log file, " + outputChangeLogFile);
        }
        catch (IOException | ParserConfigurationException e) {
            throw new LiquibaseException(e);
        }
    }

    /**
     * Performs some validation after the properties file has been loaded checking that all
     * properties required have been specified.
     *
     * @throws MojoFailureException If any property that is required has not been
     *                              specified.
     */
    @Override
    protected void checkRequiredParametersAreSpecified() throws MojoFailureException {
        super.checkRequiredParametersAreSpecified();
        if (outputChangeLogFile == null) {
            throw new MojoFailureException("The output changeLogFile must be specified.");
        }
    }

    @Override
	protected void printSettings(String indent) {
		super.printSettings(indent);
        getLog().info(indent + "defaultSchemaName: " + defaultSchemaName);
        getLog().info(indent + "diffTypes: " + diffTypes);
        getLog().info(indent + "dataDir: " + dataDir);
	}

}

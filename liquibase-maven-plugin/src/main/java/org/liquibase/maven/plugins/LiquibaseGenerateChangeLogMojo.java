package org.liquibase.maven.plugins;

import liquibase.CatalogAndSchema;
import liquibase.GlobalConfiguration;
import liquibase.Liquibase;
import liquibase.Scope;
import liquibase.database.Database;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.StandardObjectChangeFilter;
import liquibase.exception.LiquibaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.integration.commandline.CommandLineUtils;
import liquibase.util.StringUtil;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.liquibase.maven.property.PropertyElement;

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
    @PropertyElement
    protected String diffTypes;

    /**
     * Directory where insert statement csv files will be kept.
     *
     * @parameter property="liquibase.dataDir"
     */
    @PropertyElement
    protected String dataDir;

    /**
     * The author to be specified for Changesets in the generated Change Log.
     *
     * @parameter property="liquibase.changeSetAuthor"
     */
    @PropertyElement
    protected String changeSetAuthor;

    /**
     * are required. If no context is specified then ALL contexts will be executed.
     * @parameter property="liquibase.contexts" default-value=""
     */
    @PropertyElement
    protected String contexts;

    /**
     * The execution context to be used for Changesets in the generated Change Log, which can be "," separated if multiple contexts.
     *
     * @parameter property="liquibase.changeSetContext"
     */
    @PropertyElement
    protected String changeSetContext;

    /**
     * The target change log file to output to. If this is null then the output will be to the screen.
     *
     * @parameter property="liquibase.outputChangeLogFile"
     */
    @PropertyElement
    protected String outputChangeLogFile;


    /**
     * Objects to be excluded from the changelog. Example filters: "table_name", "table:main_.*", "column:*._lock, table:primary.*".
     *
     * @parameter property="liquibase.diffExcludeObjects"
     */
    @PropertyElement
    protected String diffExcludeObjects;

    /**
     * Objects to be included in the changelog. Example filters: "table_name", "table:main_.*", "column:*._lock, table:primary.*".
     *
     * @parameter property="liquibase.diffIncludeObjects"
     */
    @PropertyElement
    protected String diffIncludeObjects;

    /**
     * Specifies the a list of schemas to indicate liquibase where to apply change objects or where to read current state from
     * @parameter property="liquibase.schemas"
     */
    @PropertyElement
    protected String schemas;

    /**
     * Flag to Indicate liquibase whether or not to include schema name on changelog
     * @parameter property="liquibase.includeSchema"
     */
    @PropertyElement
    protected  Boolean includeSchema;

    /**
     * Flag to allow overwriting of output changelog file
     *
     * @parameter property="liquibase.overwriteOutputFile" default-value="false"
     */
    @PropertyElement
    protected boolean overwriteOutputFile;

    /**
     * Sets runOnChange="true" for changesets containing solely changes of these types (e.g. createView, createProcedure, ...).
     *
     * @parameter property="liquibase.runOnChangeTypes" default-value="none"
     */
    @PropertyElement
    protected String runOnChangeTypes;

    /**
     * Sets replaceIfExists="true" for changes of the supported types, at the moment they are createView and createProcedure.
     *
     * @parameter property="liquibase.replaceIfExistsTypes" default-value="none"
     */
    @PropertyElement
    protected String replaceIfExistsTypes;

    /**
     * Flag to allow adding 'OR REPLACE' option to the create view change object when generating changelog in SQL format
     *
     * @parameter property="liquibase.useOrReplaceOption" default-value="false"
     */
    @PropertyElement
    protected boolean useOrReplaceOption;

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
            DiffOutputControl diffOutputControl = new DiffOutputControl(outputDefaultCatalog, includeSchema == null ? Boolean.FALSE : includeSchema, true, null);
            if ((diffExcludeObjects != null) && (diffIncludeObjects != null)) {
                throw new UnexpectedLiquibaseException("Cannot specify both excludeObjects and includeObjects");
            }
            if (diffExcludeObjects != null) {
                diffOutputControl.setObjectChangeFilter(new StandardObjectChangeFilter(StandardObjectChangeFilter.FilterType.EXCLUDE, diffExcludeObjects));
            }
            if (diffIncludeObjects != null) {
                diffOutputControl.setObjectChangeFilter(new StandardObjectChangeFilter(StandardObjectChangeFilter.FilterType.INCLUDE, diffIncludeObjects));
            }
            if(useOrReplaceOption) {
                diffOutputControl.setReplaceIfExistsSet(true);
            }

            //
            // Set the global configuration option based on presence of the dataOutputDirectory
            //
            boolean b = dataDir != null;
            Scope.child(GlobalConfiguration.SHOULD_SNAPSHOT_DATA.getKey(), b, () -> {
            CompareControl.ComputedSchemas computedSchemas = CompareControl.computeSchemas(schemas, null, null,
                    defaultCatalogName, defaultSchemaName, null, null, database);
            CatalogAndSchema[] targetSchemas = computedSchemas.finalTargetSchemas;

                CommandLineUtils.doGenerateChangeLog(outputChangeLogFile, database, targetSchemas, StringUtil.trimToNull(diffTypes),
                        StringUtil.trimToNull(changeSetAuthor), StringUtil.trimToNull(changeSetContext), StringUtil.trimToNull(dataDir), diffOutputControl, overwriteOutputFile, runOnChangeTypes, replaceIfExistsTypes);
                getLog().info("Output written to Change Log file, " + outputChangeLogFile);
            });
        }  catch (Exception e) {
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
            throw new MojoFailureException("The outputChangeLogFile property must be specified.");
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

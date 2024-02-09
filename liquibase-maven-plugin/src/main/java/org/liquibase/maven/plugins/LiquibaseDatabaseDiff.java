// Version:   $Id: $
// Copyright: Copyright(c) 2008 Trace Financial Limited
package org.liquibase.maven.plugins;

import liquibase.CatalogAndSchema;
import liquibase.Liquibase;
import liquibase.command.CommandScope;
import liquibase.command.core.DiffCommandStep;
import liquibase.command.core.helpers.DbUrlConnectionArgumentsCommandStep;
import liquibase.command.core.helpers.PreCompareCommandStep;
import liquibase.command.core.helpers.ReferenceDbUrlConnectionCommandStep;
import liquibase.database.Database;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.ObjectChangeFilter;
import liquibase.diff.output.StandardObjectChangeFilter;
import liquibase.exception.LiquibaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.integration.commandline.CommandLineUtils;
import liquibase.resource.ResourceAccessor;
import liquibase.util.StringUtil;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.liquibase.maven.property.PropertyElement;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;

/**
 * <p>Generates a diff between the specified database and the reference database.
 * The output is either a report or a changelog depending on the value of the diffChangeLogFile parameter.</p>
 *
 * @author Peter Murray
 * @goal diff
 */
public class LiquibaseDatabaseDiff extends AbstractLiquibaseChangeLogMojo {

    /**
     * The fully qualified name of the driver class to use to connect to the reference database.
     * If this is not specified, then the {@link #driver} will be used instead.
     *
     * @parameter property="liquibase.referenceDriver"
     */
    @PropertyElement
    protected String referenceDriver;

    /**
     * The reference database URL to connect to for executing Liquibase.
     *
     * @parameter property="liquibase.referenceUrl"
     */
    @PropertyElement
    protected String referenceUrl;

    /**
     * The reference database username to use to connect to the specified database.
     *
     * @parameter property="liquibase.referenceUsername"
     */
    @PropertyElement
    protected String referenceUsername;

    /**
     * The reference database password to use to connect to the specified database. If this is
     * null then an empty password will be used.
     *
     * @parameter property="liquibase.referencePassword"
     */
    @PropertyElement
    protected String referencePassword;

    /**
     * The reference database catalog.
     *
     * @parameter property="liquibase.referenceDefaultCatalogName"
     */
    @PropertyElement
    protected String referenceDefaultCatalogName;

    /**
     * The reference database schema.
     *
     * @parameter property="liquibase.referenceDefaultSchemaName"
     */
    @PropertyElement
    protected String referenceDefaultSchemaName;
    /**
     * If this parameter is set, the changelog needed to "fix" differences between the two databases is output. If the file exists, it is appended to.
     * If this is null, a comparison report is output to stdout.
     *
     * @parameter property="liquibase.diffChangeLogFile"
     */
    @PropertyElement
    protected String diffChangeLogFile;
    /**
     * Include the catalog in the diff output? If this is null then the catalog will not be included
     *
     * @parameter property="liquibase.diffIncludeCatalog"
     */
    @PropertyElement
    protected boolean diffIncludeCatalog;
    /**
     * Include the schema in the diff output? If this is null then the schema will not be included
     *
     * @parameter property="liquibase.diffIncludeSchema"
     */
    @PropertyElement
    protected boolean diffIncludeSchema;
    /**
     * Include the tablespace in the diff output? If this is null then the tablespace will not be included
     *
     * @parameter property="liquibase.diffIncludeTablespace"
     */
    @PropertyElement
    protected boolean diffIncludeTablespace;
    /**
     * List of diff types to include in Change Log expressed as a comma separated list from: tables, views, columns, indexes, foreignkeys, primarykeys, uniqueconstraints, data.
     * If this is null then the default types will be: tables, views, columns, indexes, foreignkeys, primarykeys, uniqueconstraints
     *
     * @parameter property="liquibase.diffTypes"
     */
    @PropertyElement
    protected String diffTypes;
    /**
     * The author to be specified for Changesets in the generated Change Log.
     *
     * @parameter property="liquibase.changeSetAuthor"
     */
    @PropertyElement
    protected String changeSetAuthor;
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
     * The server id in settings.xml to use when authenticating with.
     *
     * @parameter property="liquibase.referenceServer"
     */
    @PropertyElement
    protected String referenceServer;

    /**
     *
     * Schemas on target database to use in diff.  This is a CSV list.
     *
     * @parameter property="liquibase.schemas"
     *
     */
    @PropertyElement
    protected String schemas;

    /**
     *
     * Schemas names on reference database to use in diff.  This is a CSV list.
     *
     * @parameter property="liquibase.referenceSchemas"
     *
     */
    @PropertyElement
    protected String referenceSchemas;

    /**
     *
     * Output schemas names.  This is a CSV list.
     *
     * @parameter property="liquibase.outputSchemas"
     *
     */
    @PropertyElement
    protected String outputSchemas;

    /**
     * Write the output of the diff to a file
     * <p>
     *
     * @parameter property="liquibase.outputFile"
     *
     */
    @PropertyElement
    protected String outputFile;

    /**
     * The format in which to display the diff output
     * TXT or JSON
     *
     * @parameter property="liquibase.format"
     */
    @PropertyElement
    protected String format;

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
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (this.referenceServer != null) {
            AuthenticationInfo referenceInfo = wagonManager.getAuthenticationInfo(referenceServer);
            if (referenceInfo != null) {
                referenceUsername = referenceInfo.getUserName();
                referencePassword = referenceInfo.getPassword();
            }
        }
        super.execute();
    }

    @Override
    protected void performLiquibaseTask(Liquibase liquibase) throws LiquibaseException {
        //
        // Check the Pro license if --format=JSON is specified
        //
        if (isFormattedDiff()) {
            if (format != null && ! format.equalsIgnoreCase("json")) {
                String messageString =
                        "\nWARNING: The diff command 'diff --format=" + format  + "' optional Pro parameter '--format' " +
                                "currently supports only 'TXT' or 'JSON' as values.  (Blank defaults to 'TXT')";
                throw new LiquibaseException(String.format(messageString));
            }
        }
        ClassLoader cl = null;
        ResourceAccessor resourceAccessor;
        try {
            cl = getClassLoaderIncludingProjectClasspath();
            Thread.currentThread().setContextClassLoader(cl);

            ClassLoader artifactClassLoader = getMavenArtifactClassLoader();
            resourceAccessor = getResourceAccessor(artifactClassLoader);
        } catch (Exception e) {
            throw new LiquibaseException("Could not create the class loader, " + e, e);
        }

        Database db = liquibase.getDatabase();

        try (Database referenceDatabase = CommandLineUtils.createDatabaseObject(resourceAccessor, referenceUrl, referenceUsername, referencePassword, referenceDriver, referenceDefaultCatalogName, referenceDefaultSchemaName, outputDefaultCatalog, outputDefaultSchema, null, null, propertyProviderClass, null, null, databaseChangeLogTableName, databaseChangeLogLockTableName)) {
            ReferenceDbUrlConnectionCommandStep.logMdc(referenceUrl, referenceUsername, referenceDefaultSchemaName, referenceDefaultCatalogName);

            getLog().info("Performing Diff on database " + db.toString());
            if ((diffExcludeObjects != null) && (diffIncludeObjects != null)) {
                throw new UnexpectedLiquibaseException("Cannot specify both excludeObjects and includeObjects");
            }
            ObjectChangeFilter objectChangeFilter = null;
            if (diffExcludeObjects != null) {
                objectChangeFilter = new StandardObjectChangeFilter(StandardObjectChangeFilter.FilterType.EXCLUDE, diffExcludeObjects);
            }
            if (diffIncludeObjects != null) {
                objectChangeFilter = new StandardObjectChangeFilter(StandardObjectChangeFilter.FilterType.INCLUDE, diffIncludeObjects);
            }

            CompareControl.SchemaComparison[] schemaComparisons = createSchemaComparisons(db);
            if (diffChangeLogFile != null) {
                try {
                    DiffOutputControl diffOutputControl = new DiffOutputControl(diffIncludeCatalog, diffIncludeSchema, diffIncludeTablespace, null).addIncludedSchema(new CatalogAndSchema(referenceDefaultCatalogName, referenceDefaultSchemaName));
                    diffOutputControl.setObjectChangeFilter(objectChangeFilter);
                    if(useOrReplaceOption) {
                        diffOutputControl.setReplaceIfExistsSet(true);
                    }
                    CommandLineUtils.doDiffToChangeLog(diffChangeLogFile, referenceDatabase, db, changeSetAuthor, diffOutputControl,
                            objectChangeFilter, StringUtil.trimToNull(diffTypes), schemaComparisons, runOnChangeTypes, replaceIfExistsTypes);
                    if (new File(diffChangeLogFile).exists()) {
                        getLog().info("Differences written to Change Log File, " + diffChangeLogFile);
                    }
                } catch (IOException | ParserConfigurationException e) {
                    throw new LiquibaseException(e);
                }
            } else {
                PrintStream output = createPrintStream();
                CommandScope liquibaseCommand = new CommandScope("diff");
                liquibaseCommand.setOutput(output);
                liquibaseCommand.addArgumentValue(DiffCommandStep.FORMAT_ARG, format);
                liquibaseCommand.addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, db);
                liquibaseCommand.addArgumentValue(ReferenceDbUrlConnectionCommandStep.REFERENCE_DATABASE_ARG, referenceDatabase);
                liquibaseCommand.addArgumentValue(PreCompareCommandStep.COMPARE_CONTROL_ARG, new CompareControl(schemaComparisons, diffTypes));
                liquibaseCommand.addArgumentValue(PreCompareCommandStep.OBJECT_CHANGE_FILTER_ARG, objectChangeFilter);
                if (StringUtil.isEmpty(diffTypes)) {
                    liquibaseCommand.addArgumentValue(PreCompareCommandStep.SNAPSHOT_TYPES_ARG, new Class[0]);
                } else {
                    liquibaseCommand.addArgumentValue(PreCompareCommandStep.SNAPSHOT_TYPES_ARG, DiffCommandStep.parseSnapshotTypes(diffTypes));
                }
                liquibaseCommand.execute();
            }
        }
    }

    private CompareControl.SchemaComparison[] createSchemaComparisons(Database database) {
        CompareControl.ComputedSchemas computedSchemas = CompareControl.computeSchemas(
                schemas,
                referenceSchemas,
                outputSchemas,
                defaultCatalogName, defaultSchemaName,
                referenceDefaultCatalogName, referenceDefaultSchemaName,
                database);
        PreCompareCommandStep.logMdcProperties(schemas, outputSchemas, referenceSchemas);

        return computedSchemas.finalSchemaComparisons;
    }
    private PrintStream createPrintStream() throws LiquibaseException {
        try {
            return (outputFile != null ? new PrintStream(outputFile) : System.out);
        }
        catch (FileNotFoundException fnfe) {
            throw new LiquibaseException(fnfe);
        }
    }

    private boolean isFormattedDiff() {
        return format != null && ! format.toUpperCase().equals("TXT");
    }

    @Override
    protected void printSettings(String indent) {
        super.printSettings(indent);
        getLog().info(indent + "referenceDriver: " + referenceDriver);
        getLog().info(indent + "referenceUrl: " + referenceUrl);
        getLog().info(indent + "referenceUsername: " + "*****");
        getLog().info(indent + "referencePassword: " + "*****");
        getLog().info(indent + "referenceDefaultSchema: " + referenceDefaultSchemaName);
        getLog().info(indent + "diffChangeLogFile: " + diffChangeLogFile);
    }

    @Override
    @SuppressWarnings("squid:S2068") // SONAR thinks we would hard-code passwords here.
    protected void checkRequiredParametersAreSpecified() throws MojoFailureException {
        super.checkRequiredParametersAreSpecified();

        if (referenceUrl == null) {
            throw new MojoFailureException("A reference database must be provided to perform a diff.");
        }

        if (referencePassword == null) {
            referencePassword = "";
        }
    }
}

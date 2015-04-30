package org.liquibase.maven.plugins;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.StandardObjectChangeFilter;
import liquibase.exception.LiquibaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.integration.commandline.CommandLineUtils;
import liquibase.util.StringUtils;

import org.apache.maven.plugin.MojoExecutionException;

/**
 * Generates SQL that marks all unapplied changes as applied.
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
     * @parameter expression="${liquibase.diffTypes}"
     */
    private String diffTypes;

    /**
     * Directory where insert statement csv files will be kept.
     *
     * @parameter expression="${liquibase.dataDir}"
     */
    private String dataDir;

    /**
     * The author to be specified for Change Sets in the generated Change Log.
     *
     * @parameter expression="${liquibase.changeSetAuthor}"
     */
    private String changeSetAuthor;

    /**
     * are required. If no context is specified then ALL contexts will be executed.
     * @parameter expression="${liquibase.contexts}" default-value=""
     */
    protected String contexts;

    /**
     * The execution context to be used for Change Sets in the generated Change Log, which can be "," separated if multiple contexts.
     *
     * @parameter expression="${liquibase.changeSetContext}"
     */
    private String changeSetContext;

    /**
     * The target change log file to output to. If this is null then the output will be to the screen.
     *
     * @parameter expression="${liquibase.outputChangeLogFile}"
     */
    protected String outputChangeLogFile;


    /**
     * Objects to be excluded from the changelog. Example filters: "table_name", "table:main_.*", "column:*._lock, table:primary.*".
     *
     * @parameter expression="${liquibase.diffExcludeObjects}"
     */
    protected String diffExcludeObjects;

    /**
     * Objects to be included in the changelog. Example filters: "table_name", "table:main_.*", "column:*._lock, table:primary.*".
     *
     * @parameter expression="${liquibase.diffIncludeObjects}"
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
            DiffOutputControl diffOutputControl = new DiffOutputControl(outputDefaultCatalog, outputDefaultSchema, true);
            if (diffExcludeObjects != null && diffIncludeObjects != null) {
                throw new UnexpectedLiquibaseException("Cannot specify both excludeObjects and includeObjects");
            }
            if (diffExcludeObjects != null) {
                diffOutputControl.setObjectChangeFilter(new StandardObjectChangeFilter(StandardObjectChangeFilter.FilterType.EXCLUDE, diffExcludeObjects));
            }
            if (diffIncludeObjects != null) {
                diffOutputControl.setObjectChangeFilter(new StandardObjectChangeFilter(StandardObjectChangeFilter.FilterType.INCLUDE, diffIncludeObjects));
            }

            CommandLineUtils.doGenerateChangeLog(outputChangeLogFile, database, defaultCatalogName, defaultSchemaName, StringUtils.trimToNull(diffTypes),
                    StringUtils.trimToNull(changeSetAuthor), StringUtils.trimToNull(changeSetContext), StringUtils.trimToNull(dataDir), diffOutputControl);
            getLog().info("Output written to Change Log file, " + outputChangeLogFile);
        }
        catch (IOException e) {
            throw new LiquibaseException(e);
        }
        catch (ParserConfigurationException e) {
            throw new LiquibaseException(e);
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

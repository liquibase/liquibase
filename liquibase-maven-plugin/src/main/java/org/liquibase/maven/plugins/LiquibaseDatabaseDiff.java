// Version:   $Id: $
// Copyright: Copyright(c) 2008 Trace Financial Limited
package org.liquibase.maven.plugins;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import liquibase.CatalogAndSchema;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.StandardObjectChangeFilter;
import liquibase.exception.LiquibaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.integration.commandline.CommandLineUtils;
import liquibase.util.StringUtils;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.wagon.authentication.AuthenticationInfo;

/**
 * Generates a diff between the specified database and the reference database.
 * The output is either a report or a changelog depending on the value of the diffChangeLogFile parameter.
 *
 * @author Peter Murray
 * @goal diff
 */
public class LiquibaseDatabaseDiff extends AbstractLiquibaseChangeLogMojo {

    /**
     * The fully qualified name of the driver class to use to connect to the reference database.
     * If this is not specified, then the {@link #driver} will be used instead.
     *
     * @parameter expression="${liquibase.referenceDriver}"
     */
    protected String referenceDriver;

    /**
     * The reference database URL to connect to for executing Liquibase.
     *
     * @parameter expression="${liquibase.referenceUrl}"
     */
    protected String referenceUrl;

    /**
     * The reference database username to use to connect to the specified database.
     *
     * @parameter expression="${liquibase.referenceUsername}"
     */
    protected String referenceUsername;

    /**
     * The reference database password to use to connect to the specified database. If this is
     * null then an empty password will be used.
     *
     * @parameter expression="${liquibase.referencePassword}"
     */
    protected String referencePassword;

    /**
     * The reference database catalog.
     *
     * @parameter expression="${liquibase.referenceDefaultCatalogName}"
     */
    protected String referenceDefaultCatalogName;

    /**
     * The reference database schema.
     *
     * @parameter expression="${liquibase.referenceDefaultSchemaName}"
     */
    protected String referenceDefaultSchemaName;

    /**
     * The server id in settings.xml to use when authenticating with.
     *
     * @parameter expression="${liquibase.referenceServer}"
     */
    private String referenceServer;


    /**
     * If this parameter is set, the changelog needed to "fix" differences between the two databases is output. If the file exists, it is appended to.
     * If this is null, a comparison report is output to stdout.
     *
     * @parameter expression="${liquibase.diffChangeLogFile}"
     */
    protected String diffChangeLogFile;

    /**
     * Include the catalog in the diff output? If this is null then the catalog will not be included
     *
     * @parameter expression="${liquibase.diffIncludeCatalog}"
     */
    protected boolean diffIncludeCatalog;

    /**
     * Include the schema in the diff output? If this is null then the schema will not be included
     *
     * @parameter expression="${liquibase.diffIncludeSchema}"
     */
    protected boolean diffIncludeSchema;

    /**
     * Include the tablespace in the diff output? If this is null then the tablespace will not be included
     *
     * @parameter expression="${liquibase.diffIncludeTablespace}"
     */
    protected boolean diffIncludeTablespace;
    
    /**
     * List of diff types to include in Change Log expressed as a comma separated list from: tables, views, columns, indexes, foreignkeys, primarykeys, uniqueconstraints, data.
     * If this is null then the default types will be: tables, views, columns, indexes, foreignkeys, primarykeys, uniqueconstraints
     *
     * @parameter expression="${liquibase.diffTypes}"
     */
    protected String diffTypes;

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
    public void execute() throws MojoExecutionException, MojoFailureException {
    	if(referenceServer!=null) {
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
        ClassLoader cl = null;
        try {
            cl = getClassLoaderIncludingProjectClasspath();
            Thread.currentThread().setContextClassLoader(cl);
        }
        catch (MojoExecutionException e) {
            throw new LiquibaseException("Could not create the class loader, " + e, e);
        }

        Database db = liquibase.getDatabase();
        Database referenceDatabase = CommandLineUtils.createDatabaseObject(cl, referenceUrl, referenceUsername, referencePassword, referenceDriver, referenceDefaultCatalogName, referenceDefaultSchemaName, outputDefaultCatalog, outputDefaultSchema, null, null, propertyProviderClass, null, null);

        getLog().info("Performing Diff on database " + db.toString());
        if (diffChangeLogFile != null) {
            try {
                DiffOutputControl diffOutputControl = new DiffOutputControl(diffIncludeCatalog, diffIncludeSchema, diffIncludeTablespace).addIncludedSchema(new CatalogAndSchema(referenceDefaultCatalogName, referenceDefaultSchemaName));
                if (diffExcludeObjects != null && diffIncludeObjects != null) {
                    throw new UnexpectedLiquibaseException("Cannot specify both excludeObjects and includeObjects");
                }
                if (diffExcludeObjects != null) {
                    diffOutputControl.setObjectChangeFilter(new StandardObjectChangeFilter(StandardObjectChangeFilter.FilterType.EXCLUDE, diffExcludeObjects));
                }
                if (diffIncludeObjects != null) {
                    diffOutputControl.setObjectChangeFilter(new StandardObjectChangeFilter(StandardObjectChangeFilter.FilterType.INCLUDE, diffIncludeObjects));
                }

                CommandLineUtils.doDiffToChangeLog(diffChangeLogFile, referenceDatabase, db, diffOutputControl, StringUtils.trimToNull(diffTypes));
                getLog().info("Differences written to Change Log File, " + diffChangeLogFile);
            }
            catch (IOException e) {
                throw new LiquibaseException(e);
            }
            catch (ParserConfigurationException e) {
                throw new LiquibaseException(e);
            }
        } else {
            CommandLineUtils.doDiff(referenceDatabase, db, StringUtils.trimToNull(diffTypes));
        }
    }

    @Override
    protected void printSettings(String indent) {
        super.printSettings(indent);
        getLog().info(indent + "referenceDriver: " + referenceDriver);
        getLog().info(indent + "referenceUrl: " + referenceUrl);
        getLog().info(indent + "referenceUsername: " + referenceUsername);
        getLog().info(indent + "referencePassword: " + referencePassword);
        getLog().info(indent + "referenceDefaultSchema: " + referenceDefaultSchemaName);
        getLog().info(indent + "diffChangeLogFile: " + diffChangeLogFile);
    }

    @Override
    protected void checkRequiredParametersAreSpecified() throws MojoFailureException {
        super.checkRequiredParametersAreSpecified();

        if (referenceUrl == null) {
            throw new MojoFailureException("A reference database must be provided to perform a diff.");
        }

        if (referencePassword == null) {
            referencePassword = "";
        }
    }

    @Override
    protected boolean isPromptOnNonLocalDatabase() {
        return false;
  }
}
